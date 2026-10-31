/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.tools.ToolProvider;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.*;

/**
 * Test class of {@link PluginsClassLoader}
 */
class PluginsClassLoaderTest {

	protected static final String USER_HOME_DIRECTORY = "target/test-classes/home-test";

	@BeforeEach
	void cleanHome() throws IOException {
		FileUtils.deleteDirectory(new File(new File(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER), PluginsClassLoader.EXPORT_DIR));
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

				final var pluginsJavadoc = classLoader.getInstalledPlugins(new HashMap<>(), true);
				Assertions.assertEquals(1, pluginsJavadoc.size());
				Assertions.assertEquals("plugin-javadoc-0", pluginsJavadoc.get("plugin-javadoc"));

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

	/**
	 * A plug-in jar of the plug-ins directory overrides the same class embedded in the parent class-path (a plug-in
	 * shipped inside the application WAR): classes are resolved child-first, like the resources.
	 */
	@Test
	void childFirstClassLoading() throws Exception {
		final var home = Files.createTempDirectory("ligoj-home");
		try {
			final var plugins = Files.createDirectories(home.resolve(PluginsClassLoader.PLUGINS_DIR));
			// A plug-in flavor of ShadowedSample, compiled on the fly and packaged in a versioned jar
			final var src = Files.createDirectories(home.resolve("src/org/ligoj/bootstrap/core/plugin"))
					.resolve("ShadowedSample.java");
			Files.writeString(src, "package org.ligoj.bootstrap.core.plugin;\npublic class ShadowedSample {"
					+ " public static final String ORIGIN = \"plugin\"; }\n");
			final var classes = Files.createDirectories(home.resolve("classes"));
			Assertions.assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d",
					classes.toString(), src.toString()));
			final var jar = plugins.resolve("plugin-shadow-1.0.0.jar");
			try (var out = new JarOutputStream(Files.newOutputStream(jar))) {
				out.putNextEntry(new JarEntry("org/ligoj/bootstrap/core/plugin/ShadowedSample.class"));
				out.write(Files.readAllBytes(classes.resolve("org/ligoj/bootstrap/core/plugin/ShadowedSample.class")));
				out.closeEntry();
			}

			System.setProperty("ligoj.home", home.toString());
			try (var classLoader = new PluginsClassLoader()) {
				final var shadowed = classLoader.loadClass(ShadowedSample.class.getName());
				Assertions.assertNotSame(ShadowedSample.class, shadowed);
				Assertions.assertSame(classLoader, shadowed.getClassLoader());
				Assertions.assertEquals("plugin", shadowed.getField("ORIGIN").get(null));
				// Classes absent from the plug-ins still come from the parent
				Assertions.assertSame(PluginsClassLoader.class, classLoader.loadClass(PluginsClassLoader.class.getName()));
				Assertions.assertSame(String.class, classLoader.loadClass("java.lang.String"));
				// A second lookup returns the same defined class, also when the class is resolved
				Assertions.assertSame(shadowed, classLoader.loadClass(ShadowedSample.class.getName()));
				Assertions.assertSame(shadowed, classLoader.loadClass(ShadowedSample.class.getName(), true));
			}
		} finally {
			System.clearProperty("ligoj.home");
			FileUtils.deleteDirectory(home.toFile());
		}
	}

	/**
	 * Write a plugin jar with an embedded Maven descriptor.
	 */
	private void writePlugin(final Path plugins, final String artifact, final String pom) throws IOException {
		try (var out = new JarOutputStream(Files.newOutputStream(plugins.resolve(artifact + "-1.0.0.jar")))) {
			// The descriptor of an embedded library, then a resource: not the descriptor of this plugin
			out.putNextEntry(new JarEntry("META-INF/maven/org.sample/library/pom.xml"));
			out.closeEntry();
			out.putNextEntry(new JarEntry("sample/" + artifact + "/pom.xml"));
			out.closeEntry();
			out.putNextEntry(new JarEntry("META-INF/maven/org.ligoj.plugin/" + artifact + "/pom.xml"));
			out.write(pom.getBytes(StandardCharsets.UTF_8));
			out.closeEntry();
		}
	}

	private String pom(final String... dependencies) {
		return "<project>\n<groupId>org.ligoj.plugin</groupId>\n<dependencies>\n" + String.join("\n", dependencies)
				+ "\n</dependencies>\n<build><plugins/></build>\n</project>";
	}

	private String parent(final String artifact) {
		return "<dependency><groupId>org.ligoj.plugin</groupId><artifactId>" + artifact
				+ "</artifactId><scope>provided</scope></dependency>";
	}

	private PluginsClassLoader newLoader(final Path home) throws IOException, NoSuchAlgorithmException {
		System.setProperty(PluginsClassLoader.HOME_DIR_PROPERTY, home.toString());
		return new PluginsClassLoader();
	}

	private boolean isLoaded(final PluginsClassLoader loader, final String artifact) {
		return java.util.Arrays.stream(loader.getURLs()).anyMatch(u -> u.toString().contains("/" + artifact + "-1.0.0.jar"));
	}

	/**
	 * A plugin with a missing parent stays out of the class-path, like the plugins depending on it, instead of
	 * stopping the application at the class scanning. The other plugins are still loaded.
	 */
	@Test
	void missingParentExcludesThePlugin() throws Exception {
		final var home = Files.createTempDirectory("ligoj-home");
		try {
			final var plugins = Files.createDirectories(home.resolve(PluginsClassLoader.PLUGINS_DIR));
			writePlugin(plugins, "plugin-qa-sonarqube", pom(parent("plugin-qa")));
			writePlugin(plugins, "plugin-qa-sonarqube-extra", pom(parent("plugin-qa-sonarqube")));
			writePlugin(plugins, "plugin-build", pom());
			writePlugin(plugins, "plugin-build-jenkins", pom(parent("plugin-build")));
			try (var loader = newLoader(home)) {
				Assertions.assertFalse(isLoaded(loader, "plugin-qa-sonarqube"));
				Assertions.assertFalse(isLoaded(loader, "plugin-qa-sonarqube-extra"));
				Assertions.assertTrue(isLoaded(loader, "plugin-build"));
				Assertions.assertTrue(isLoaded(loader, "plugin-build-jenkins"));

				// Still reported as installed, and the reason is known
				Assertions.assertTrue(loader.getInstalledPlugins().containsKey("plugin-qa-sonarqube"));
				Assertions.assertEquals(java.util.List.of("plugin-qa"), loader.getMissingDependencies().get("plugin-qa-sonarqube"));
				Assertions.assertEquals(java.util.List.of("plugin-qa-sonarqube"), loader.getMissingDependencies().get("plugin-qa-sonarqube-extra"));
				Assertions.assertFalse(loader.getMissingDependencies().containsKey("plugin-build-jenkins"));
			}
		} finally {
			System.clearProperty(PluginsClassLoader.HOME_DIR_PROPERTY);
			FileUtils.deleteDirectory(home.toFile());
		}
	}

	/**
	 * Only the provided plugins are required. A parent embedded in the application class-path is available.
	 */
	@Test
	void requiredDependenciesOnly() throws Exception {
		final var home = Files.createTempDirectory("ligoj-home");
		final var embedded = Files.createTempDirectory("ligoj-embedded");
		final var previous = Thread.currentThread().getContextClassLoader();
		try {
			final var maven = Files.createDirectories(embedded.resolve("META-INF/maven/org.ligoj.plugin/plugin-embedded"));
			Files.writeString(maven.resolve("pom.xml"), pom());
			final var plugins = Files.createDirectories(home.resolve(PluginsClassLoader.PLUGINS_DIR));
			writePlugin(plugins, "plugin-a", pom(parent("plugin-embedded"),
					"<dependency><groupId>org.ligoj.plugin</groupId><artifactId>plugin-test-only</artifactId><scope>test</scope></dependency>",
					"<dependency><groupId>org.ligoj.plugin</groupId><artifactId>plugin-compile</artifactId></dependency>",
					"<dependency><groupId>org.sample</groupId><artifactId>provided-library</artifactId><scope>provided</scope></dependency>"));
			writePlugin(plugins, "plugin-b", "<project><dependencyManagement><dependencies>" + parent("plugin-managed-only")
					+ "</dependencies></dependencyManagement></project>");
			writePlugin(plugins, "plugin-c", "<project><dependencies><dependency><groupId>${project.groupId}</groupId>"
					+ "<artifactId>plugin-b</artifactId><scope>provided</scope></dependency></dependencies></project>");
			writePlugin(plugins, "plugin-d", "<project><dependencies><dependency><groupId>${project.groupId}</groupId>"
					+ "<artifactId>plugin-unknown</artifactId><scope>provided</scope></dependency></dependencies></project>");
			writePlugin(plugins, "plugin-e", "not xml");
			Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[]{embedded.toUri().toURL()}, previous));
			try (var loader = newLoader(home)) {
				Assertions.assertTrue(isLoaded(loader, "plugin-a"));
				Assertions.assertTrue(isLoaded(loader, "plugin-b"));
				Assertions.assertTrue(isLoaded(loader, "plugin-c"));
				Assertions.assertFalse(isLoaded(loader, "plugin-d"));

				// An unreadable descriptor does not exclude a plugin: only a missing parent does
				Assertions.assertTrue(isLoaded(loader, "plugin-e"));
				Assertions.assertEquals(java.util.Set.of("plugin-d"), loader.getMissingDependencies().keySet());
			}
		} finally {
			Thread.currentThread().setContextClassLoader(previous);
			System.clearProperty(PluginsClassLoader.HOME_DIR_PROPERTY);
			FileUtils.deleteDirectory(home.toFile());
			FileUtils.deleteDirectory(embedded.toFile());
		}
	}

	/**
	 * Without check, for a parent without Maven descriptor such as a module opened in an IDE, the plugin is loaded.
	 */
	@Test
	void dependencyCheckDisabled() throws Exception {
		final var home = Files.createTempDirectory("ligoj-home");
		try {
			final var plugins = Files.createDirectories(home.resolve(PluginsClassLoader.PLUGINS_DIR));
			writePlugin(plugins, "plugin-qa-sonarqube", pom(parent("plugin-qa")));
			System.setProperty(PluginsClassLoader.DEPENDENCY_CHECK_PROPERTY, "false");
			try (var loader = newLoader(home)) {
				Assertions.assertTrue(isLoaded(loader, "plugin-qa-sonarqube"));
				Assertions.assertTrue(loader.getMissingDependencies().isEmpty());
			}
		} finally {
			System.clearProperty(PluginsClassLoader.DEPENDENCY_CHECK_PROPERTY);
			System.clearProperty(PluginsClassLoader.HOME_DIR_PROPERTY);
			FileUtils.deleteDirectory(home.toFile());
		}
	}

	/**
	 * A plugin file that cannot be read as a jar has no known dependency: this check does not exclude it.
	 */
	@Test
	void readDependenciesUnreadableJar() throws Exception {
		final var home = Files.createTempDirectory("ligoj-home");
		try {
			Files.createDirectories(home.resolve(PluginsClassLoader.PLUGINS_DIR));
			try (var loader = newLoader(home)) {
				Assertions.assertTrue(loader.readDependencies("plugin-any", home.resolve("missing.jar")).isEmpty());
			}
		} finally {
			System.clearProperty(PluginsClassLoader.HOME_DIR_PROPERTY);
			FileUtils.deleteDirectory(home.toFile());
		}
	}

	@Test
	void forcedHome() throws IOException, NoSuchAlgorithmException {
		try {
			System.setProperty("ligoj.home", USER_HOME_DIRECTORY + "/.ligoj");
			try (var classLoader = checkClassLoader()) {
				Assertions.assertTrue(classLoader.isEnabled());
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
		Assertions.assertEquals("Z0000012Z0000034Z0000056Z0000789", PluginsClassLoader.toExtendedVersion("12.34.56.789"));
		Assertions.assertEquals("Z0000012Z000003bZ000005AZ0000000", PluginsClassLoader.toExtendedVersion("12.3b.5A"));
	}

	@Test
	void getInstance() throws IOException {
		var old = Thread.currentThread().getContextClassLoader();
		try (var cl = new URLClassLoader(new URL[0], mock(PluginsClassLoader.class))) {
			Thread.currentThread().setContextClassLoader(cl);
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
				try (var classLoader = new PluginsClassLoader() {
					@Override
					protected void copy(final Path from, final Path to) throws IOException {
						throw new IOException();
					}
				}) {
					Assertions.assertTrue(classLoader.isEnabled());
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
				classLoader.copyExportedResources("plugin-foo", new File(USER_HOME_DIRECTORY, ".ligoj/plugins/plugin-foo-1.0.1.jar").toPath());
				var exported = new File(USER_HOME_DIRECTORY, ".ligoj/export/export.txt");
				Assertions.assertTrue(exported.exists());
				FileUtils.write(exported, "value", StandardCharsets.UTF_8);

				// Copy again without error or overwrite
				classLoader.copyExportedResources("plugin-foo", new File(USER_HOME_DIRECTORY, ".ligoj/plugins/plugin-foo-1.0.1.jar").toPath());

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
		IOUtils.toString(classLoader.getResourceAsStream("home-test/.ligoj/plugins/plugin-foo-1.0.1.jar"), StandardCharsets.UTF_8);
		Assertions.assertEquals("FOO", IOUtils.toString(classLoader.getResourceAsStream("plugin-foo.txt"), StandardCharsets.UTF_8));

		final var export = new File(USER_HOME_DIRECTORY + "/.ligoj/export");
		Assertions.assertTrue(export.exists());
		Assertions.assertTrue(export.isDirectory());
		Assertions.assertTrue(new File(export, "export.txt").exists());
		Assertions.assertTrue(new File(export, "export.txt").isFile());
		Assertions.assertEquals("EXPORT", FileUtils.readFileToString(new File(export, "export.txt"), StandardCharsets.UTF_8));
		return classLoader;
	}

	@Test
	void getResource() throws IOException, NoSuchAlgorithmException {
		try (var classLoader = new PluginsClassLoader()) {
			// Unknown resource
			Assertions.assertNull(classLoader.getResource("_$not_exist$_"));
			final var resources = classLoader.getResources("_$not_exist$_");
			Assertions.assertFalse(resources.hasMoreElements());
			Assertions.assertThrows(NoSuchElementException.class, resources::nextElement);

			// Local only resource
			Assertions.assertNotNull(classLoader.getResource("test.properties"));
			Assertions.assertTrue(classLoader.getResources("test.properties").hasMoreElements());

			// Other CP only resource
			Assertions.assertNotNull(classLoader.getResource("maven-buildinfo.properties"));
			Assertions.assertTrue(classLoader.getResources("maven-buildinfo.properties").hasMoreElements());
		}
	}

	@Test
	void getBootstrapCodeError() throws IOException, NoSuchAlgorithmException {
		try (var classLoader = new PluginsClassLoader()) {
			var url = mock(URL.class);
			Assertions.assertThrows(NullPointerException.class, () -> classLoader.getBootstrapCode(url));

			var input = mock(InputStream.class);
			doReturn(input).when(url).openStream();
			Assertions.assertThrows(NullPointerException.class, () -> classLoader.getBootstrapCode(url));

			doThrow(new IOException()).when(input).readAllBytes();
			Assertions.assertThrows(IOException.class, () -> classLoader.getBootstrapCode(url));

			doReturn("ok".getBytes(StandardCharsets.UTF_8)).when(input).readAllBytes();
			Assertions.assertEquals("ok", classLoader.getBootstrapCode(url));

			doThrow(new IOException()).when(input).close();
			Assertions.assertThrows(IOException.class, () -> classLoader.getBootstrapCode(url));
		}
	}

	@Test
	void getBootstrapCode() throws IOException, NoSuchAlgorithmException {
		try (var classLoader = new PluginsClassLoader()) {
			var url = mock(URL.class);
			var input = mock(InputStream.class);
			doReturn(input).when(url).openStream();
			doReturn("ok".getBytes(StandardCharsets.UTF_8)).when(input).readAllBytes();
			Assertions.assertEquals("ok", classLoader.getBootstrapCode(url));
		}

	}
}
