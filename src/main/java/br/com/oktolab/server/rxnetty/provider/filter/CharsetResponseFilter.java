package br.com.oktolab.server.rxnetty.provider.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
public class CharsetResponseFilter implements ContainerResponseFilter {

	final String APPLICATION_JSON_WITH_UTF8_CHARSET = MediaType.APPLICATION_JSON + ";charset=" + java.nio.charset.StandardCharsets.UTF_8;

	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {
		String contentType = responseContext.getHeaderString("Content-Type");
		if (contentType != null && !contentType.contains("charset")) {
			MediaType mediaType = responseContext.getMediaType();
			responseContext.getHeaders().putSingle("Content-Type", mediaType.toString() + ";charset=UTF-8");
		}
	}
	
}
