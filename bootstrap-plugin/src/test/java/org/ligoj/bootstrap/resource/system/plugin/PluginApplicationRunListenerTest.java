/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jetty.util.thread.ThreadClassLoaderScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.plugin.PluginsClassLoader;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

/**
 * Test class of {@link PluginApplicationRunListener}
 */
class PluginApplicationRunListenerTest {

	@Test
	void noPluginClassLoader() throws IOException {
		try (ThreadClassLoaderScope scope = new ThreadClassLoaderScope(new URLClassLoader(new URL[0]))) {
			new PluginApplicationRunListener(Mockito.mock(SpringApplication.class), new String[0]).starting();
		}
	}

	@Test
	void pluginClassLoader() throws IOException {
		final var scope = new ThreadClassLoaderScope(new PluginsClassLoader());
		try {
			final var listener = new PluginApplicationRunListener(Mockito.mock(SpringApplication.class), new String[0]);
			listener.starting();
			Assertions.assertTrue(listener.getOrder() < 0);
			listener.environmentPrepared(null);
			listener.contextPrepared(null);
			listener.contextLoaded(null);
			listener.started(null);
			listener.running(null);
			listener.failed(null, null);
		} finally {
			scope.close();
		}
	}
}
