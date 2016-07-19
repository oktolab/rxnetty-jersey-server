package br.com.oktolab.server.rxnetty.provider.filter;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.netflix.config.ConfigurationManager;

@Provider
public class CorsInterceptor implements ContainerResponseFilter {

    private String corsAllowedOrigin = ConfigurationManager.getConfigInstance().getString("cors.allowed.origin", "*");

    private final Integer corsPreflightMaxAgeInSeconds = 30 * 60;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
    	Boolean corsAllowed = ConfigurationManager.getConfigInstance().getBoolean("cors.allowed", Boolean.TRUE);
    	if (!corsAllowed) {
    		return;
    	}
		responseContext.getHeaders().add("Access-Control-Allow-Origin", this.corsAllowedOrigin);
		responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
		responseContext.getHeaders().add("Access-Control-Allow-Credentials", "false");
		List<String> allowedHeaders = requestContext.getHeaders().get("Access-Control-Request-Headers");
		if (allowedHeaders != null && allowedHeaders.size() > 0) {
			StringBuilder headerallowed = new StringBuilder();
			for (String allowedHeader : allowedHeaders) {
				headerallowed.append(allowedHeader + ", ");
			}
			responseContext.getHeaders().add("Access-Control-Allow-Headers", headerallowed.toString());
		}
		responseContext.getHeaders().add("Access-Control-Max-Age", this.corsPreflightMaxAgeInSeconds);
    }

}
