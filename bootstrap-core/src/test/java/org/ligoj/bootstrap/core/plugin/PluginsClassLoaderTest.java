/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test class of {@link PluginsClassLoader}
 */
class PluginsClassLoaderTest {

	protected static final String USER_HOME_DIRECTORY = "target/test-classes/home-test";

	@BeforeEach
	void cleanHome() throws IOException {
		FileUtils.deleteDirectory(new File(new File(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER),
				PluginsClassLoader.EXPORT_DIR));
		System.clearProperty("project.version.digest");
	}

	@Test
	void getInstalledPlugins() throws IOException, NoSuchAlgorithmException {
		final var oldHome = System.getProperty("user.home");
		try {
			System.setProperty("user.home", USER_HOME_DIRECTORY);
			try (var classLoader = checkClassLoader()) {
				// Nothing to do
				final var plugins = classLoader.getInstalledPlugins();
				Assertions.assertEquals(3, plugins.size());
				Assertions.assertEquals("plugin-foo-Z0000001Z0000000Z0000001Z0000000", plugins.get("plugin-foo"));
				Assertions.assertEquals("plugin-bar-Z0000001Z0000000Z0000000Z0000000", plugins.get("plugin-bar"));
				Assertions.assertEquals("plugin-sample-Z0000002Z0000000Z0000000Z0000000", plugins.get("plugin-sample"));
				Assertions.assertEquals("wMxLd+H9uVdM4fRKRhOQpA==", classLoader.getDigestVersion());
				Assertions.assertEquals("wMxLd+H9uVdM4fRKRhOQpA==", System.getProperty("project.version.digest"));
				Assertions.assertEquals("foo\n", System.getProperty("project.bootstrap.private"));

			}
		} finally {
			System.setProperty("user.home", oldHome);
		}
	}

	@Test
	void safeMode() throws IOException, NoSuchAlgorithmException {
		final var old = System.getProperty("ligoj.plugin.enabled");
		try {
			System.setProperty("ligoj.plugin.enabled", "false");
			try (var classLoader = new PluginsClassLoader()) {
				Assertions.assertFalse(classLoader.isEnabled());

				// Check the home is in the class-path
				final var homeUrl = classLoader.getURLs()[0];
				Assertions.assertTrue(homeUrl.getFile().endsWith("/"));

				// Check the plug-in is in the class-path
				Assertions.assertEquals(1, classLoader.getURLs().length);
			}
		} finally {
			if (old == null) {
				System.clearProperty("ligoj.plugin.enabled");
			} else {
				System.setProperty("ligoj.plugin.enabled", old);
			}
		}
	}

	@Test
	void forcedHome() throws IOException, NoSuchAlgorithmException {
		try {
			System.setProperty("ligoj.home", USER_HOME_DIRECTORY + "/.ligoj");
			try (var classLoader = checkClassLoader()) {
				// Nothing to do
			}
		} finally {
			System.clearProperty("ligoj.home");
		}
	}

	@Test
	void getInstanceNull() {
		Assertions.assertNull(PluginsClassLoader.getInstance());
	}

	@Test
	void toExtendedVersion() {
		Assertions.assertEquals("Z0000000Z0000000Z0000000Z0000000", PluginsClassLoader.toExtendedVersion(null));
		Assertions.assertEquals("Z0000000Z0000000Z0000000Z0000000", PluginsClassLoader.toExtendedVersion(""));
		Assertions.assertEquals("Z0000001Z0000000Z0000000Z0000000", PluginsClassLoader.toExtendedVersion("1.0"));
		Assertions.assertEquals("Z0000001Z0000002Z0000003Z0000004", PluginsClassLoader.toExtendedVersion("1.2.3.4"));
		Assertions.assertEquals("Z0000012Z0000034Z0000056Z0000789",
				PluginsClassLoader.toExtendedVersion("12.34.56.789"));
		Assertions.assertEquals("Z0000012Z000003bZ000005AZ0000000", PluginsClassLoader.toExtendedVersion("12.3b.5A"));
	}

	@Test
	void getInstance() {
		var old = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread()
					.setContextClassLoader(new URLClassLoader(new URL[0], Mockito.mock(PluginsClassLoader.class)));
			Assertions.assertNotNull(PluginsClassLoader.getInstance());
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
	}

	@Test
	void forcedHomeTwice() throws Exception {
		try {
			System.setProperty("ligoj.home", USER_HOME_DIRECTORY + "/.ligoj");
			try (var classLoader = checkClassLoader()) {
				Assertions.assertNotNull(classLoader.getHomeDirectory());
				Assertions.assertNotNull(classLoader.getPluginDirectory());
			}
		} finally {
			System.clearProperty("ligoj.home");
		}
	}

	@Test
	void toFile() throws IOException, NoSuchAlgorithmException {
		final var file = new File(USER_HOME_DIRECTORY, ".ligoj/service-id/foo/bar.log");
		final var subscriptionParent = new File(USER_HOME_DIRECTORY, ".ligoj/service-id");
		FileUtils.deleteQuietly(subscriptionParent);
		Assertions.assertFalse(subscriptionParent.exists());
		Assertions.assertFalse(file.exists());
		try {
			System.setProperty("ligoj.home", USER_HOME_DIRECTORY + "/.ligoj");
			try (var classLoader = checkClassLoader()) {
				final var cFile = classLoader.toPath("service-id", "foo", "bar.log").toFile();
				Assertions.assertTrue(subscriptionParent.exists());
				Assertions.assertTrue(cFile.getParentFile().exists());
				Assertions.assertTrue(file.getParentFile().exists());
			}
			Assertions.assertFalse(file.exists());
		} finally {
			System.clearProperty("ligoj.home");
		}
	}

	@Test
	void copyFailed() throws IOException {
		final var refError = new AtomicReference<PluginsClassLoader>();
		try {
			System.setProperty("ligoj.home", USER_HOME_DIRECTORY + "/.ligoj");
			Assertions.assertThrows(PluginException.class, () -> {
				try (PluginsClassLoader classLoader = new PluginsClassLoader() {
					@Override
					protected void copy(final Path from, final Path to) throws IOException {
						throw new IOException();
					}
				}) {
					// Ignore
				}
			});
		} finally {
			System.clearProperty("ligoj.home");
			if (refError.get() != null) {
				refError.get().close();
			}
		}
	}

	@Test
	void copyAlreadyExists() throws IOException, NoSuchAlgorithmException {
		final var refError = new AtomicReference<PluginsClassLoader>();
		try {
			System.setProperty("ligoj.home", USER_HOME_DIRECTORY + "/.ligoj");
			try (PluginsClassLoader classLoader = new PluginsClassLoader() {
				@Override
				protected void copy(final Path from, final Path to) throws IOException {
					if (!from.toString().endsWith("/export")) {
						FileUtils.touch(to.toFile());
					}
				}
			}) {
				classLoader.copyExportedResources("plugin-foo",
						new File(USER_HOME_DIRECTORY, ".ligoj/plugins/plugin-foo-1.0.1.jar").toPath());
				var exported = new File(USER_HOME_DIRECTORY, ".ligoj/export/export.txt");
				Assertions.assertTrue(exported.exists());
				FileUtils.write(exported, "value", StandardCharsets.UTF_8);

				// Copy again without error or overwrite
				classLoader.copyExportedResources("plugin-foo",
						new File(USER_HOME_DIRECTORY, ".ligoj/plugins/plugin-foo-1.0.1.jar").toPath());

				Assertions.assertTrue(exported.exists());
				Assertions.assertEquals("value", FileUtils.readFileToString(exported, StandardCharsets.UTF_8));

			}
		} finally {
			System.clearProperty("ligoj.home");
			if (refError.get() != null) {
				refError.get().close();
			}
		}
	}

	private PluginsClassLoader checkClassLoader() throws IOException, NoSuchAlgorithmException {
		final var classLoader = new PluginsClassLoader();
		Assertions.assertEquals(4, classLoader.getURLs().length);

		// Check the home is in the class-path
		final var homeUrl = classLoader.getURLs()[0];
		Assertions.assertTrue(homeUrl.getFile().endsWith("/"));

		// Check the plug-in is in the class-path
		final var pluginTestUrl = classLoader.getURLs()[2];
		Assertions.assertTrue(pluginTestUrl.getFile().endsWith("plugin-foo-1.0.1.jar"));

		// Check the JAR is readable
		try (var pluginTestUrlStream = pluginTestUrl.openStream()) {
			Assertions.assertNotNull(pluginTestUrlStream);
		}

		// Check the content of the plug-in is resolvable from the class loader
		IOUtils.toString(classLoader.getResourceAsStream("home-test/.ligoj/plugins/plugin-foo-1.0.1.jar"),
				StandardCharsets.UTF_8);
		Assertions.assertEquals("FOO",
				IOUtils.toString(classLoader.getResourceAsStream("plugin-foo.txt"), StandardCharsets.UTF_8));

		final var export = new File(USER_HOME_DIRECTORY + "/.ligoj/export");
		Assertions.assertTrue(export.exists());
		Assertions.assertTrue(export.isDirectory());
		Assertions.assertTrue(new File(export, "export.txt").exists());
		Assertions.assertTrue(new File(export, "export.txt").isFile());
		Assertions.assertEquals("EXPORT",
				FileUtils.readFileToString(new File(export, "export.txt"), StandardCharsets.UTF_8));
		return classLoader;
	}
}
