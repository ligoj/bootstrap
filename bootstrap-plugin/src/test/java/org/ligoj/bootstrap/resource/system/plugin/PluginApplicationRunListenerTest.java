/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.thread.ThreadClassLoaderScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.plugin.PluginsClassLoader;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;

/**
 * Test class of {@link PluginApplicationRunListener}
 */
class PluginApplicationRunListenerTest {

	@Test
	void noPluginClassLoader() {
		try (ThreadClassLoaderScope ignored = new ThreadClassLoaderScope(new URLClassLoader(new URL[0]))) {
			new PluginApplicationRunListener(Mockito.mock(SpringApplication.class)).starting(null);
		}
		Assertions.assertEquals("app", Thread.currentThread().getContextClassLoader().getName());
	}

	@Test
	void pluginClassLoader() throws IOException, NoSuchAlgorithmException {
		try (ThreadClassLoaderScope ignored = new ThreadClassLoaderScope(new PluginsClassLoader())) {
			final var listener = new PluginApplicationRunListener(Mockito.mock(SpringApplication.class));
			listener.starting(null);
			Assertions.assertTrue(listener.getOrder() < 0);
			listener.environmentPrepared(null, null);
			listener.contextPrepared(null);
			listener.contextLoaded(null);
			listener.started(null, null);
			listener.ready(null, null);
			listener.failed(null, null);
		}
		Assertions.assertEquals("app", Thread.currentThread().getContextClassLoader().getName());
	}

	@Test
	void pluginClassLoaderFail() {
		final var oldValue = System.getProperty(PluginsClassLoader.HOME_DIR_PROPERTY);
		try (ThreadClassLoaderScope ignored = new ThreadClassLoaderScope(new URLClassLoader(new URL[0]))) {
			System.setProperty(PluginsClassLoader.HOME_DIR_PROPERTY, StringUtils.repeat("../_not_valid_/", 257));
			new PluginApplicationRunListener(Mockito.mock(SpringApplication.class));
		} finally {
			if (oldValue == null) {
				System.clearProperty(PluginsClassLoader.HOME_DIR_PROPERTY);
			} else {
				System.setProperty(PluginsClassLoader.HOME_DIR_PROPERTY, oldValue);
			}
		}
		Assertions.assertNull(PluginsClassLoader.getInstance());
	}
}
