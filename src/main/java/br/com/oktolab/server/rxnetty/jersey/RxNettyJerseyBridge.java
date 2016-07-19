package br.com.oktolab.server.rxnetty.jersey;

import static com.netflix.config.ConfigurationManager.getConfigInstance;

import br.com.oktolab.server.rxnetty.ConfigurationConstants;
import br.com.oktolab.server.rxnetty.jersey.blocking.KaryonJersey2Module;

public class RxNettyJerseyBridge extends KaryonJersey2Module {
	
//	private static final Logger LOG = LoggerFactory.getLogger(KaryonJerseyModuleImpl.class);
	
//	@Override
//	protected void configureServer() {
//		final int jerseyPort = getConfigInstance().getInt(ConfigurationConstants.KEY_JERSEY_PORT, 8082);
//		final int jerseyPoolSize = getConfigInstance().getInt(ConfigurationConstants.KEY_JERSEY_THREAD_POOL_SIZE, 100);
//		HttpServerConfigBuilder serverConfig = server();
//		serverConfig.port(jerseyPort).threadPoolSize(jerseyPoolSize);
//	}
	
}
