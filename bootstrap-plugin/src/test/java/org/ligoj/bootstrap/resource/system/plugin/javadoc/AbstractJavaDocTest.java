/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import org.ligoj.bootstrap.core.plugin.PluginsClassLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base  class for Javadoc related test classes
 */
public class AbstractJavaDocTest {


	private static final String USER_HOME_DIRECTORY = "target/test-classes/home-test";

	protected List<URL> newJavadocUrls() throws IOException {
		final var jarPath = Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR).resolve("plugin-javadoc.jar");
		final var pluginFiles = Map.of("plugin-javadoc", jarPath.toString());
		final var versionFileToPath = Map.of(jarPath.toString(), jarPath);
		final var javadocUrls = new ArrayList<URL>();
		for (final var path : pluginFiles.values()) {
			final var javadocPath = versionFileToPath.get(path);
			javadocUrls.add(javadocPath.toUri().toURL());
		}
		return javadocUrls;
	}
}
