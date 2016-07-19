package br.com.oktolab.server;

import com.google.inject.Module;
import com.netflix.config.DynamicPropertyFactory;

import br.com.oktolab.server.rxnetty.RxNettyJerseyServer;

public class ApplicationBootstrap {
	public static RxNettyJerseyServer run(final Class<?> applicationClazz) {
		return run(applicationClazz, new Module[]{});
	}
	
	public static RxNettyJerseyServer run(final Class<?> applicationClazz, final Module... modules) {
		System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");
//		final Module[] modulesConcat = getModules(modules);
//		KaryonServer karyonServer = Karyon.forApplication(applicationClazz, modulesConcat);
//		karyonServer.start();
		return new RxNettyJerseyServer();
	}
	
	public static void runAndWaitTillShutdown(final Class<?> applicationClazz) throws Exception {
		run(applicationClazz, new Module[]{}).start();//waitTillShutdown();
	}
	
	public static void runAndWaitTillShutdown(final Class<?> applicationClazz, final Module... modules) {
		run(applicationClazz, modules).waitTillShutdown();
	}
}
