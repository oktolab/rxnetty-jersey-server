package br.com.oktolab.server.rxnetty;

import com.google.inject.Module;
import com.netflix.config.DynamicPropertyFactory;

public class ApplicationBootstrap {
	
	public static HttpServer run(final Class<?> applicationClazz) {
		return run(applicationClazz, new Module[]{});
	}
	
	public static HttpServer run(final Class<?> applicationClazz, final Module... modules) {
		System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");
//		final Module[] modulesConcat = getModules(modules);
//		KaryonServer karyonServer = Karyon.forApplication(applicationClazz, modulesConcat);
//		karyonServer.start();
		return new HttpServer();
	}
	
	public static void runAndWaitTillShutdown(final Class<?> applicationClazz) throws Exception {
		run(applicationClazz, new Module[]{}).start();//waitTillShutdown();
	}
	
	public static void runAndWaitTillShutdown(final Class<?> applicationClazz, final Module... modules) {
		run(applicationClazz, modules).waitTillShutdown();
	}

	
	
}
