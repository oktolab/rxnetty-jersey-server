package br.com.oktolab.server.provider;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/healthcheck")
public class HealthCheckRest {

	@GET
	public String healthCheck() {
		return "OK";
	}
}
