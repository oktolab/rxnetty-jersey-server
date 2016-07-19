package br.com.oktolab.server.rxnetty;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

import br.com.oktolab.server.rxnetty.jersey.Jersey2BasedRouter;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;

public class RxNettyJerseyServer {
	
	private List<Module> modules;
	private Injector injector;
	private LifecycleManager manager;
	
	public void start() throws Exception {
		injector = LifecycleInjector.builder()
			    .withModules(modules).build().createInjector();
		manager = injector.getInstance(LifecycleManager.class);
		manager.start();
		io.reactivex.netty.protocol.http.server.HttpServer<ByteBuf, ByteBuf> httpServer = RxNetty.createHttpServer(7020, new Jersey2BasedRouter(injector));
		httpServer.start();
		httpServer.waitTillShutdown();
	}

	public void waitTillShutdown() {
		// TODO Auto-generated method stub
		
	}
	
	public void shutdown() {
		manager.close();
	}
}
