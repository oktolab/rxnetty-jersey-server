package br.com.oktolab.server.rxnetty.jersey.blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import br.com.oktolab.server.rxnetty.jersey.PropertiesBasedResourceConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpRequestHeaders;
import io.reactivex.netty.protocol.http.server.HttpResponseHeaders;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class Jersey2BasedRouter implements RequestHandler<ByteBuf, ByteBuf> {

    private static final Logger LOG = LoggerFactory.getLogger(Jersey2BasedRouter.class);

    private final PropertiesBasedResourceConfig resourceConfig;
    private final Injector injector;
    private ApplicationHandler application;
    
    /**
     * Default dummy security context.
     */
    private static final SecurityContext DEFAULT_SECURITY_CONTEXT = new SecurityContext() {

        @Override
        public boolean isUserInRole(final String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getAuthenticationScheme() {
            return null;
        }
    };

    public Jersey2BasedRouter() {
        this(null);
    }

    @Inject
    public Jersey2BasedRouter(Injector injector) {
        this.injector = injector;
        ServiceIteratorProviderImpl.registerWithJersey();
        resourceConfig = new PropertiesBasedResourceConfig();
        resourceConfig.register(RolesAllowedDynamicFeature.class);
    }
    
    @PostConstruct
    public void start() {
        NettyContainer container = ContainerFactory.createContainer(NettyContainer.class, resourceConfig);
        Application app = container.getApplication();
        application =  new ApplicationHandler(app);
        LOG.info("Started Jersey based request router.");
    }

    @PreDestroy
    public void stop() {
        LOG.info("Stopped Jersey based request router.");
        // application.destroy(); ? TODO
    }
    
    @Override
    public Observable<Void> handle(final HttpServerRequest<ByteBuf> request, final HttpServerResponse<ByteBuf> response) {
    	try {
    		final InputStream requestData = new HttpContentInputStream(response.getAllocator(), request.getContent());
    		
    		URI baseUri = new URI("/");
    		URI uri = new URI(request.getUri());
    		PropertiesDelegate delegate = resourceConfig.getPropertiesDelegate();
    		
    		if ("/favicon.ico".equals(uri.getPath())) {
    			// TODO GET BEHAVIOR FROM MODULE CLASS
    			request.ignoreContent();
    			return response.close();
    		}
    		
    		ContainerRequest containerRequest = new ContainerRequest(baseUri, uri, request.getHttpMethod().name(), 
    				DEFAULT_SECURITY_CONTEXT, delegate);
    		
    		final HttpRequestHeaders headers = request.getHeaders();
    		if (headers != null && !headers.isEmpty()) {
    			Set<String> names = headers.names();
    			for (String key : names) {
    				containerRequest.getHeaders().add(key, headers.get(key));
				}
    		}
    		containerRequest.setEntityStream(requestData);
    		final ContainerResponseWriter containerResponse = bridgeResponse(response);
    		containerRequest.setWriter(containerResponse);
    		return Observable.create(new Observable.OnSubscribe<Void>() {
    			@Override
    			public void call(Subscriber<? super Void> subscriber) {
    				try {
    					application.handle(containerRequest);
    					subscriber.onCompleted();
    				} catch (Exception e) {
    					LOG.error("Failed to handle request.", e);
    					subscriber.onError(e);
    				} finally {
    					try {
    						requestData.close();
    					}
    					catch( IOException e ) {
    						// NOOP
    					}
    				}
    			}
    		}).doOnTerminate(new Action0() {
    			@Override
    			public void call() {
    				response.close(true); // Since this runs in a different thread, it needs an explicit flush,
    				// else the LastHttpContent will never be flushed and the client will not finish.
    			}
    		}).subscribeOn(Schedulers.io());
    	} catch (Exception e) {
    		LOG.error("Error!", e);
    		return null;
    	}

    }
    
    ContainerResponseWriter bridgeResponse(final HttpServerResponse<ByteBuf> serverResponse) {
        return new ContainerResponseWriter() {

            private final ByteBuf contentBuffer = serverResponse.getChannel().alloc().buffer();

            @Override
            public OutputStream writeResponseStatusAndHeaders(
            		final long contentLength, final ContainerResponse response) throws ContainerException {
                int responseStatus = response.getStatus();
                serverResponse.setStatus(HttpResponseStatus.valueOf(responseStatus));
                HttpResponseHeaders responseHeaders = serverResponse.getHeaders();
                for(Map.Entry<String, List<Object>> header : response.getHeaders().entrySet()){
                    responseHeaders.setHeader(header.getKey(), header.getValue());
                }
                return new ByteBufOutputStream(contentBuffer);
            }

			@Override
			public boolean suspend(long timeOut, TimeUnit timeUnit,
					TimeoutHandler timeoutHandler) {
				return false;
			}

			@Override
			public void setSuspendTimeout(long timeOut, TimeUnit timeUnit)
					throws IllegalStateException {
			}

			@Override
			public void commit() {
				if (contentBuffer.isReadable()) {
					serverResponse.writeAndFlush(contentBuffer);
				} else {
					contentBuffer.release();
					serverResponse.flush();
				}
			}

			@Override
			public void failure(Throwable error) {
				LOG.error("Error ContainerResponseWriter ", error);
				try {
					contentBuffer.release();
				} catch (Exception e) {}
			}

			@Override
			public boolean enableResponseBuffering() {
				return false;
			}
        };
    }

}
