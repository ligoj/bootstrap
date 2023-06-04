/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.eclipse.jetty.util.thread.ThreadClassLoaderScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.MatcherUtil;
import org.ligoj.bootstrap.core.dao.csv.CsvForJpa;
import org.ligoj.bootstrap.core.plugin.FeaturePlugin;
import org.ligoj.bootstrap.core.plugin.PluginListener;
import org.ligoj.bootstrap.core.plugin.PluginVo;
import org.ligoj.bootstrap.core.plugin.PluginsClassLoader;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.ligoj.bootstrap.dao.system.SystemPluginRepository;
import org.ligoj.bootstrap.model.system.*;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.ligoj.bootstrap.resource.system.plugin.repository.Artifact;
import org.ligoj.bootstrap.resource.system.session.ApplicationSettings;
import org.ligoj.bootstrap.resource.system.session.SessionSettings;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Test class of {@link SystemPluginResource}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
class SystemPluginResourceTest extends org.ligoj.bootstrap.AbstractServerTest {

	private static final String USER_HOME_DIRECTORY = "target/test-classes/home-test";

	/**
	 * File used to be created when a plug-in is downloaded from this test class
	 */
	private static final File TEMP_FILE = Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER,
			PluginsClassLoader.PLUGINS_DIR, "plugin-iam-node-test.jar").toFile();

	private SystemPluginResource resource;

	@Autowired
	private SystemPluginRepository repository;

	@Autowired
	private org.ligoj.bootstrap.resource.system.plugin.repository.CentralRepositoryManager centralRepositoryManager;

	@Autowired
	private RestartEndpoint restartEndpoint;

	@Autowired
	private ApplicationSettings applicationSettings;

	@Autowired
	private ConfigurationResource configuration;

	@BeforeEach
	void prepareData() throws IOException {
		FileUtils.deleteQuietly(TEMP_FILE);
		persistEntities("csv-test", SystemConfiguration.class);
		clearAllCache();
		resource = mockCentral("search.json");
		configuration.put("ligoj.plugin.ignore", " plugin-sample-ignore , any");
		System.clearProperty("plugins.repository-manager.nexus.artifact.url");
		System.clearProperty("plugins.repository-manager.nexus.search.url");
	}

	@AfterEach
	void cleanSingleton() {
		destroySingleton("restartEndpoint");
	}

	@Test
	void findAllCentralOnline() throws IOException {
		configuration.delete("plugins.repository-manager.central.search.url");
		Assertions.assertTrue("1.0.0".compareTo(resource.getRepositoryManager("central").getLastPluginVersions()
				.get("plugin-iam-node").getVersion()) <= 0);
	}

	@Test
	void getRepositoryManager() throws IOException {
		Assertions.assertTrue(resource.getRepositoryManager("not-exist").getLastPluginVersions().isEmpty());
		Assertions.assertNull(resource.getRepositoryManager("not-exist").getArtifactInputStream("any", "1.2.3"));
		resource.getRepositoryManager("not-exist").invalidateLastPluginVersions();
		Assertions.assertEquals("empty", resource.getRepositoryManager("not-exist").getId());
	}

	@Test
	void findAllNewVersion() throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();
		final var listener = Mockito.mock(PluginListener.class);
		registerSingleton("mockPluginListener", listener);
		try {
			// This plug-in is available in the remote storage with a newer version
			Assertions.assertEquals("0.0.1", findAll("0.0.0.8").getNewVersion());
			Mockito.verify(listener, Mockito.atLeastOnce()).toVo();
			Mockito.verify(listener, Mockito.atLeastOnce()).fillVo(ArgumentMatchers.any(), ArgumentMatchers.any(),
					ArgumentMatchers.any());
		} finally {
			destroySingleton("sampleService");
			destroySingleton("sampleTool");
			destroySingleton("mockPluginListener");
		}
	}

	@Test
	void findAllInstalledNextSameVersion() throws IOException {
		final var resource = mockCentral("search.json");
		final var currentVersion = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-foo".equals(p.getPlugin().getArtifact())).findFirst().get().getPlugin()
				.getVersion();
		resource.getPluginClassLoader().getInstalledPlugins().put("plugin-foo",
				"plugin-foo-" + PluginsClassLoader.toExtendedVersion(currentVersion));

		final var pluginVo = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-foo".equals(p.getPlugin().getArtifact())).findFirst().get();
		Assertions.assertNotNull(pluginVo.getLatestLocalVersion());
		Assertions.assertEquals(currentVersion, pluginVo.getPlugin().getVersion());
		Assertions.assertFalse(pluginVo.isDeleted());
		Assertions.assertNull(pluginVo.getNewVersion());
		Assertions.assertEquals("Foo", pluginVo.getName());
	}
	@Test
	void findAllInstalledNextNewPlugin() throws IOException {
		final var resource = mockCentral("search.json");
		resource.getPluginClassLoader().getInstalledPlugins().put("plugin-sample",
				"plugin-sample-Z0000001Z0000002Z0000003Z0000004");

		final var pluginVo = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-sample".equals(p.getPlugin().getArtifact())).findFirst().get();
		Assertions.assertEquals("1.2.3.4", pluginVo.getLatestLocalVersion());
		Assertions.assertNull(pluginVo.getPlugin().getVersion());
		Assertions.assertFalse(pluginVo.isDeleted());
		Assertions.assertNull(pluginVo.getNewVersion());
		Assertions.assertEquals("plugin-sample", pluginVo.getName());
	}

	@Test
	void findAllInstalledNextUpdate() throws IOException {
		final var resource = mockCentral("search.json");
		resource.getPluginClassLoader().getInstalledPlugins().put("plugin-bar",
				"plugin-bar-Z0000123Z0000004Z0000005Z0000000");

		final var pluginVo = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-bar".equals(p.getPlugin().getArtifact())).findFirst().get();
		Assertions.assertEquals("123.4.5", pluginVo.getLatestLocalVersion());
		Assertions.assertNull(pluginVo.getPlugin().getVersion());
		Assertions.assertNull(pluginVo.getNewVersion());
		Assertions.assertFalse(pluginVo.isDeleted());
		Assertions.assertEquals("plugin-bar", pluginVo.getName());
	}

	@Test
	void findAllSameVersion() throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		try {
			// This plug-in is available in the remote storage with a newer version
			Assertions.assertNull(findAll("0.0.1").getNewVersion());
		} finally {
			destroySingleton("sampleService");
			destroySingleton("sampleTool");
		}
	}

	@Test
	void isDeleted() {
		final var plugin = new PluginVo();
		plugin.setLocation("any");
		Assertions.assertTrue(resource.isDeleted(plugin));
	}

	@Test
	void isDeletedExist() {
		final var plugin = new PluginVo();
		plugin.setLocation(USER_HOME_DIRECTORY);
		Assertions.assertFalse(resource.isDeleted(plugin));
	}

	@Test
	void toTrimmedVersion() {
		Assertions.assertEquals("1.2.3.4", resource.toTrimmedVersion("plugin-sample-Z0000001Z0000002Z0000003Z0000004"));
		Assertions.assertEquals("1.2.3", resource.toTrimmedVersion("plugin-sample-Z0000001Z0000002Z0000003Z0000000"));
		Assertions.assertEquals("1.2.3", resource.toTrimmedVersion("plugin-sample-Z0000001Z0000002Z0000003"));
		Assertions.assertEquals("1.2.3.4", resource.toTrimmedVersion("Z0000001Z0000002Z0000003Z0000004"));
		Assertions.assertEquals("1.2.3", resource.toTrimmedVersion("Z0000001Z0000002Z0000003Z0000000"));
		Assertions.assertEquals("1.2.3-SNAPSHOT",
				resource.toTrimmedVersion("plugin-sample-Z0000001Z0000002Z0000003SNAPSHOT"));
		Assertions.assertEquals("1.2.3-SNAPSHOT", resource.toTrimmedVersion("1.2.3-SNAPSHOT"));
		Assertions.assertEquals("1.2.3.4", resource.toTrimmedVersion("1.2.3.4"));
		Assertions.assertEquals("1.2.30", resource.toTrimmedVersion("1.2.30"));
		Assertions.assertEquals("1.2.3", resource.toTrimmedVersion("1.2.3.0"));
		Assertions.assertEquals("1.2.0", resource.toTrimmedVersion("1.2.0"));
		Assertions.assertEquals("1.2", resource.toTrimmedVersion("1.2"));
		Assertions.assertEquals("1.2.3", resource.toTrimmedVersion("1.2.3.0"));
		Assertions.assertEquals("0.2.3", resource.toTrimmedVersion("0000.02.0003."));
	}

	@Test
	void autoUpdateNoPluginMatch() throws IOException {
		Assertions.assertEquals(0, mockCentral("search.json").autoUpdate());
	}

	@Test
	void refreshPluginsAutoInstall() throws Exception {
		configuration.put("ligoj.plugin.install", "plugin-sample");
		final var check = new AtomicBoolean(false);
		final var resource = new SystemPluginResource() {
			@Override
			public int autoUpdate() {
				return 0;
			}

			@Override
			public int autoInstall(Set<String> plugins) {
				Assertions.assertEquals("plugin-sample", plugins.iterator().next());
				return plugins.size();
			}

			@Override
			public void restart() {
				check.set(true);
			}
		};
		applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
		resource.refreshPlugins(null);

		// 1 install, restart needed
		Assertions.assertTrue(check.get());
	}

	@Test
	void refreshPluginsAutoInstallAlreadyInstalled() throws Exception {
		configuration.put("ligoj.plugin.install", "plugin-foo");
		final var check = new AtomicBoolean(false);
		final var resource = new SystemPluginResource() {
			@Override
			public int autoUpdate() {
				return 0;
			}

			@Override
			public int autoInstall(Set<String> plugins) {
				Assertions.assertEquals("plugin-foo", plugins.iterator().next());
				return 0;
			}

			@Override
			public void restart() {
				check.set(true);
			}
		};
		applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
		resource.refreshPlugins(new ContextRefreshedEvent(applicationContext));

		// No update, no restart needed
		Assertions.assertFalse(check.get());
	}

	@Test
	void autoInstallNoPlugin() throws IOException {
		Assertions.assertEquals(0, mockCentral("search.json").autoInstall(Collections.emptySet()));
	}

	@Test
	void autoInstallNotExists() throws IOException {
		final var plugins = new HashSet<String>();
		plugins.add("plugin-unknown");
		Assertions.assertEquals(0, mockCentral("search.json").autoInstall(plugins));
	}

	@Test
	void autoInstall() throws IOException {
		final var plugins = new HashSet<String>();
		plugins.add("plugin-sample");
		Assertions.assertEquals(1, mockCentral("search.json").autoInstall(plugins));
	}

	@Test
	void autoInstallAlreadyInstalled() throws IOException {
		final var plugins = new HashSet<String>();
		plugins.add("plugin-foo");
		Assertions.assertEquals(0, mockCentral("search-foo.json").autoInstall(plugins));
	}

	@Test
	void autoUpdateNoNewVersion() throws IOException {
		Assertions.assertEquals(0, mockCentral("search-foo.json").autoUpdate());
	}

	@Test
	void autoUpdateNewVersion() throws IOException {
		Assertions.assertEquals(1, mockCentral("search-bar.json").autoUpdate());
	}

	private SystemPluginResource mockCentral(final String body) throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/" + body).getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		final var pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		Mockito.when(pluginsClassLoader.getHomeDirectory())
				.thenReturn(Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER));
		Mockito.when(pluginsClassLoader.getPluginDirectory()).thenReturn(
				Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR));
		final var map = new HashMap<String, String>();
		map.put("plugin-foo", "plugin-foo-Z0000001Z0000000Z0000001Z0000000");
		map.put("plugin-bar", "plugin-bar-Z0000001Z0000000Z0000000Z0000000");
		Mockito.when(pluginsClassLoader.getInstalledPlugins()).thenReturn(map);
		final var pluginResource = new SystemPluginResource() {
			@Override
			protected PluginsClassLoader getPluginClassLoader() {
				return pluginsClassLoader;
			}

			@Override
			public void install(final String artifact, final String repository) {
				// Ignore
			}
		};
		applicationContext.getAutowireCapableBeanFactory().autowireBean(pluginResource);
		return pluginResource;
	}

	private PluginVo findAll(final String version) throws IOException {
		registerSingleton("sampleService", new SampleService());
		final var pluginId = new SystemPlugin();
		pluginId.setVersion(version);
		pluginId.setKey("service:sample");
		pluginId.setType("SERVICE");
		pluginId.setArtifact("plugin-sample");
		repository.saveAndFlush(pluginId);

		final var plugins = filter(resource.findAll("central"));
		Assertions.assertEquals(3, plugins.size()); // "foo", "bar", "Sample"

		// External plug-in service
		final var plugin2 = plugins.get(2);
		Assertions.assertEquals("service:sample", plugin2.getId());
		Assertions.assertEquals("Sample", plugin2.getName());
		Assertions.assertNull(plugin2.getVendor());
		Assertions.assertFalse(plugin2.getLocation().endsWith(".jar"));
		Assertions.assertEquals("SERVICE", plugin2.getPlugin().getType());

		Assertions.assertEquals(version, plugin2.getPlugin().getVersion());
		return plugin2;

	}

	/*
	 * Ignore plugin-ui runtime (issue in Eclipse)
	 */
	private List<PluginVo> filter(final List<PluginVo> plugins) {
		return plugins.stream().filter(p -> !"feature:ui".equals(p.getId())).toList();
	}

	@Test
	void findAllOrphan() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, SecurityException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		try (var scope = new ThreadClassLoaderScope(new URLClassLoader(
				new URL[] { Thread.currentThread().getContextClassLoader()
						.getResource("home-test/.ligoj/plugins/plugin-bar-1.0.0.jar") },
				Thread.currentThread().getContextClassLoader()))) {

			// Register a feature from a JAR
			registerSingleton("barResource",
					scope.getScopedClassLoader().loadClass("org.ligoj.app.plugin.bar.BarResource").getConstructors()[0]
							.newInstance());
			findAllOrphanInternal();
		} finally {
			destroySingleton("barResource");
		}
	}

	@Test
	void findAllJustInstalled() throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		try (var scope = new ThreadClassLoaderScope(new URLClassLoader(
				new URL[] { Thread.currentThread().getContextClassLoader()
						.getResource("home-test/.ligoj/plugins/plugin-bar-1.0.0.jar") },
				Thread.currentThread().getContextClassLoader()))) {

			// Register a feature from a JAR
			registerSingleton("barResource",
					scope.getScopedClassLoader().loadClass("org.ligoj.app.plugin.bar.BarResource").getConstructors()[0]
							.newInstance());
			final var plugin = new SystemPlugin();
			plugin.setKey("feature:bar");
			plugin.setVersion("1.0.0");
			plugin.setArtifact("plugin-bar");
			plugin.setType("FEATURE");
			repository.saveAndFlush(plugin);

			final var plugins = filter(resource.findAll("central"));
			Assertions.assertEquals(2, plugins.size());

			// Plug-in from the API
			final var plugin0 = plugins.get(0);
			Assertions.assertEquals("feature:bar", plugin0.getId());
			Assertions.assertEquals("BAR", plugin0.getName());
			Assertions.assertEquals("COMPANY", plugin0.getVendor());
			Assertions.assertNull(plugin0.getNewVersion());
			Assertions.assertTrue(plugin0.getLocation().endsWith(".jar"));
			Assertions.assertEquals("1.0.0", plugin0.getPlugin().getVersion());
			Assertions.assertEquals("FEATURE", plugin0.getPlugin().getType());

			// Plug-in (feature) embedded in the current project
			final var plugin1 = plugins.get(1);
			Assertions.assertEquals("feature:foo", plugin1.getId());
			Assertions.assertEquals("Foo", plugin1.getName());
			Assertions.assertNull(plugin1.getVendor());
			Assertions.assertFalse(plugin1.getLocation().endsWith(".jar"));
			Assertions.assertNotNull(plugin1.getPlugin().getVersion());
			Assertions.assertEquals("FEATURE", plugin1.getPlugin().getType());
		} finally {
			destroySingleton("barResource");
		}
	}

	private void findAllOrphanInternal() throws IOException {

		// A tool where plug-in is no more available -> will not be returned
		final var orphanPlugin = new SystemPlugin();
		orphanPlugin.setKey("any");
		orphanPlugin.setArtifact("plugin-any");
		orphanPlugin.setType("TOOL");
		orphanPlugin.setVersion("1.1");
		repository.saveAndFlush(orphanPlugin);

		final var plugins = filter(resource.findAll("central"));
		Assertions.assertEquals(2, plugins.size());

		// Plug-in in the classpath
		final var plugin0 = plugins.get(0);
		Assertions.assertEquals("feature:foo", plugin0.getId());
		Assertions.assertEquals("Foo", plugin0.getName());
		Assertions.assertNull(plugin0.getVendor());
		Assertions.assertNull(plugin0.getNewVersion());
		Assertions.assertFalse(plugin0.getLocation().endsWith(".jar"));
		Assertions.assertNotNull(plugin0.getPlugin().getVersion());
		Assertions.assertEquals("FEATURE", plugin0.getPlugin().getType());

		// Plug-in present in the plug-ins directory but not in the class-path
		final var plugin1 = plugins.get(1);
		Assertions.assertEquals("plugin-bar", plugin1.getId());
		Assertions.assertEquals("plugin-bar", plugin1.getName());
		Assertions.assertNull(plugin1.getVendor());
		Assertions.assertNull(plugin1.getLocation());
		Assertions.assertNull(plugin1.getPlugin().getVersion());
		Assertions.assertNull(plugin1.getPlugin().getType());
	}

	@Test
	void configurePluginInstall() {
		// The class-loader of this mock corresponds to the one related to SampleService
		// So corresponds to API jar and not this project
		final var service1 = new SampleService();
		resource.configurePluginInstall(service1);
		registerSingleton("mockPluginListener", new MockPluginListener());
		try {
			Assertions.assertEquals("FEATURE", repository.findByExpected("key", "service:sample").getType());
			Assertions.assertNotNull(repository.findByExpected("key", "service:sample").getVersion());

			final var service2 = new SampleTool1();
			resource.configurePluginInstall(service2);

			// Uninstall the plug-in from the plug-in registry
			repository.deleteAllBy("key", "service:sample:tool1");

			// reinstall
			resource.configurePluginInstall(service2);
		} finally {
			destroySingleton("mockPluginListener");
		}
	}

	@Test
	void configurePluginEntities() throws IOException {
		// The class-loader of this mock corresponds to the one related to SampleService
		// So corresponds to API jar and not this project
		final var service1 = new SampleService();
		resource.configurePluginInstall(service1);
		registerSingleton("mockPluginListener", new MockPluginListener());
		try {
			Assertions.assertEquals("FEATURE", repository.findByExpected("key", "service:sample").getType());
			Assertions.assertNotNull(repository.findByExpected("key", "service:sample").getVersion());

			final var service2 = new SampleTool1();
			resource.configurePluginInstall(service2);

			// Uninstall the plug-in from the plug-in registry
			repository.deleteAllBy("key", "service:sample:tool1");

			// reinstall
			resource.configurePluginEntities(service2, Collections.singletonList(SystemBench.class));
		} finally {
			destroySingleton("mockPluginListener");
		}
	}

	@Test
	void configurePluginInstallManagedEntitiesNotSameClassloader() {
		final var service1 = Mockito.mock(FeaturePlugin.class);
		Mockito.when(service1.getKey()).thenReturn("service:sample");
		Mockito.when(service1.getInstalledEntities()).thenReturn(Collections.singletonList(SystemBench.class));
		Assertions.assertThrows(TechnicalException.class, () -> resource.configurePluginInstall(service1));
	}

	@Test
	void configurePluginInstallError() {
		final var service1 = Mockito.mock(FeaturePlugin.class);
		Mockito.when(service1.getInstalledEntities()).thenThrow(ValidationJsonException.class);
		Assertions.assertThrows(TechnicalException.class, () -> resource.configurePluginInstall(service1));
	}

	@Test
	void manifestData() {
		Assertions.assertTrue(
				Integer.class.getModule().getDescriptor().rawVersion().get().matches("\\d+(\\.\\d+\\..*)?(-ea)?$"));
		Assertions.assertEquals("java.base", Integer.class.getModule().getName());
	}

	@Test
	void configurePluginUpdate() {
		final var service1 = new SampleService();
		final var plugin = new SystemPlugin();
		plugin.setVersion("old version");
		resource.configurePluginUpdate(service1, plugin);
		Assertions.assertTrue(plugin.getVersion().matches("\\d{4}-\\d{2}-\\d{2}T.*"));
	}

	@Test
	void getVersion() {
		// Version is resolved from the date
		Assertions.assertTrue(resource.getVersion(new SampleService()).startsWith("20"));
	}

	@Test
	void getVersionIOException() {
		Assertions.assertEquals("?", new SystemPluginResource() {
			@Override
			protected String getLastModifiedTime(final FeaturePlugin plugin) throws IOException {
				throw new IOException();
			}
		}.getVersion(Mockito.mock(FeaturePlugin.class)));
	}

	@Test
	void getVersionURISyntaxException() {
		Assertions.assertEquals("?", new SystemPluginResource() {
			@Override
			protected String getLastModifiedTime(final FeaturePlugin plugin) throws URISyntaxException {
				throw new URISyntaxException("input", "reason");
			}
		}.getVersion(Mockito.mock(FeaturePlugin.class)));
	}

	@Test
	void refreshPlugins() throws Exception {
		final var event = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		registerSingleton("mockPluginListener", new MockPluginListener());
		try {
			resource.refreshPlugins(event);
		} finally {
			destroySingleton("mockPluginListener");
		}
	}

	@Test
	void refreshPluginsVetoInstall() throws Exception {
		final var event = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		var listener = Mockito.mock(PluginListener.class);
		Mockito.when(listener.install(ArgumentMatchers.any())).thenReturn(false);
		registerSingleton("mockPluginListener", listener);
		// Add a plug-in is an initial version
		registerSingleton("sampleService", new SampleService() {
			@Override
			public String getVersion() {
				return "1.1";
			}
		});
		try {
			resource.refreshPlugins(event);

			// Feature is not installed
		} finally {
			destroySingleton("mockPluginListener");
			destroySingleton("sampleService");
		}
	}

	@Test
	void refreshPluginsAutoUpdate() throws Exception {
		configuration.put("ligoj.plugin.update", "true");
		final var check = new AtomicBoolean(false);
		final var resource = new SystemPluginResource() {
			@Override
			public int autoUpdate() {
				return 1;
			}

			@Override
			public void restart() {
				check.set(true);
			}
		};
		applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
		resource.refreshPlugins(null);

		// 1 update, restart needed
		Assertions.assertTrue(check.get());
	}

	@Test
	void refreshPluginsAutoUpdateNoUpdate() throws Exception {
		configuration.put("ligoj.plugin.update", "true");
		final var check = new AtomicBoolean(false);
		final var resource = new SystemPluginResource() {
			@Override
			public int autoUpdate() {
				return 0;
			}

			@Override
			public void restart() {
				check.set(true);
			}
		};
		applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
		resource.refreshPlugins(new ContextRefreshedEvent(applicationContext));

		// No update, no restart needed
		Assertions.assertFalse(check.get());
	}

	@Test
	void refreshPluginsUpdate() throws Exception {
		try {
			// Precondition
			Assertions.assertNull(repository.findBy("key", "service:sample"));

			// Add a plug-in is an initial version
			registerSingleton("sampleService", new SampleService() {
				@Override
				public String getVersion() {
					return "1.1";
				}
			});

			final var event = Mockito.mock(ContextRefreshedEvent.class);
			Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
			resource.refreshPlugins(event);
			Assertions.assertEquals("1.1", repository.findByExpected("key", "service:sample").getVersion());

			// Add a plug-in is a different version
			destroySingleton("sampleService");
			registerSingleton("sampleService", new SampleService() {
				@Override
				public String getVersion() {
					return "2.0";
				}
			});
			resource.refreshPlugins(event);
			Assertions.assertEquals("2.0", repository.findByExpected("key", "service:sample").getVersion());
		} finally {
			destroySingleton("sampleService");
		}

	}

	@Test
	void configurePluginEntityNotFound() throws MalformedURLException {
		final var resource = new SystemPluginResource();
		final var urls = Arrays.stream(new URL[] { new URL("file://tmp") });
		Assertions.assertThrows(TechnicalException.class,
				() -> resource.configurePluginEntity(urls, SystemUser.class, "---"));
	}

	@Test
	void configurePluginEntityFromJar() throws IOException {
		try (var scope = new ThreadClassLoaderScope(new URLClassLoader(
				new URL[] { Thread.currentThread().getContextClassLoader()
						.getResource("home-test/.ligoj/plugins/plugin-bar-1.0.0.jar") },
				Thread.currentThread().getContextClassLoader()))) {
			final var url = Thread.currentThread().getContextClassLoader()
					.getResource("csv/sample-business-entity.csv");
			final var pluginResource = new SystemPluginResource();
			pluginResource.em = Mockito.mock(EntityManager.class);
			pluginResource.csvForJpa = Mockito.mock(CsvForJpa.class);
			pluginResource.configurePluginEntity(Arrays.stream(new URL[] { url }), SampleBusinessEntity.class,
					url.getPath());
		}
	}

	@Test
	void configurePluginEntityFromProject() throws IOException {
		final var url = Thread.currentThread().getContextClassLoader()
				.getResource("csv-test/sample-business-entity.csv");
		final var pluginResource = new SystemPluginResource();
		pluginResource.em = Mockito.mock(EntityManager.class);
		pluginResource.csvForJpa = csvForJpa;
		pluginResource.configurePluginEntity(Arrays.stream(new URL[] { url }), SampleBusinessEntity.class,
				url.toString());
	}

	@Test
	void configurePluginEntitySystemUser() throws IOException {
		final var url = Thread.currentThread().getContextClassLoader().getResource("csv-test/sample-system-user.csv");
		final var pluginResource = new SystemPluginResource();
		pluginResource.em = Mockito.mock(EntityManager.class);
		var query = Mockito.mock(Query.class);
		Mockito.doReturn(query).when(pluginResource.em).createQuery(Mockito.anyString());
		Mockito.doReturn(query).when(query).setParameter(Mockito.eq("value"), Mockito.anyString());
		Mockito.doReturn(new ArrayList<SystemUser>()).when(query).getResultList();

		pluginResource.csvForJpa = csvForJpa;
		pluginResource.configurePluginEntity(Arrays.stream(new URL[] { url }), SystemUser.class, url.toString());
	}

	@Test
	void installNotExists() {
		final var resource = newPluginResourceInstall();
		Assertions.assertThrows(ValidationJsonException.class, () -> resource.install("any", "central"));
	}

	@Test
	void installNotExistsVersion() {
		final var resource = newPluginResourceInstall();
		MatcherUtil.assertThrows(Assertions.assertThrows(ValidationJsonException.class,
				() -> resource.install("any", "dummy", "central")), "artifact", "cannot-be-installed");
	}

	@Test
	void installCentralOnline() throws IOException {
		configuration.delete("plugins.repository-manager.central.artifact.url");
		configuration.delete("plugins.repository-manager.central.search.url");
		newPluginResourceInstall().install("plugin-iam-node", "central");
		Assertions.assertTrue(TEMP_FILE.exists());
	}

	@Test
	void upload() throws IOException {
		final var input = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
		newPluginResourceInstall().upload(input, "plugin-sample", "1.2.9");
		Assertions.assertTrue(TEMP_FILE.exists());
		Assertions.assertEquals("test", FileUtils.readFileToString(TEMP_FILE, StandardCharsets.UTF_8));
	}

	@Test
	void installCentral() throws IOException {
		httpServer.stubFor(get(urlEqualTo("/maven2/org/ligoj/plugin/plugin-sample/0.0.1/plugin-sample-0.0.1.jar"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody(IOUtils.toByteArray(
						new ClassPathResource("mock-server/maven-repo/plugin-sample-0.0.1.jar").getInputStream()))));
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		newPluginResourceInstall().install("plugin-sample", "central");
		Assertions.assertTrue(TEMP_FILE.exists());
	}

	@Test
	void installNexusOnline() throws IOException {
		configuration.delete("plugins.repository-manager.nexus.artifact.url");
		configuration.delete("plugins.repository-manager.nexus.search.url");
		newPluginResourceInstall().install("plugin-iam-node", "nexus");
		Assertions.assertTrue(TEMP_FILE.exists());
	}

	@Test
	void installNexus() throws IOException {
		httpServer.stubFor(get(urlEqualTo(
				"/service/local/repositories/releases/content/org/ligoj/plugin/plugin-sample/0.0.1/plugin-sample-0.0.1.jar"))
						.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
								.withBody(IOUtils.toByteArray(
										new ClassPathResource("mock-server/nexus-repo/plugin-sample-0.0.1.jar")
												.getInputStream()))));
		httpServer.stubFor(get(urlEqualTo(
				"/service/local/lucene/search?g=org.ligoj.plugin&collapseresults=true&repositoryId=releases&p=jar&c=sources"))
						.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
								.withBody(IOUtils.toString(
										new ClassPathResource("mock-server/nexus-repo/search.json").getInputStream(),
										StandardCharsets.UTF_8))));
		httpServer.start();

		newPluginResourceInstall().install("plugin-sample", "nexus");
		Assertions.assertTrue(TEMP_FILE.exists());
	}

	@Test
	void getPluginClassLoaderOutOfClassLoader() {
		Assertions.assertNull(new SystemPluginResource().getPluginClassLoader());
	}

	@Test
	void restart() throws InterruptedException {
		// Difficult to test...
		resource.restart();
		Thread.sleep(100);
		Mockito.verify(restartEndpoint).restart();
	}

	@Test
	void getPluginClassLoader() {
		final var pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		try (var scope = new ThreadClassLoaderScope(new URLClassLoader(new URL[0], pluginsClassLoader))) {
			Assertions.assertNotNull(resource.getPluginClassLoader());
		}
	}

	private SystemPluginResource newPluginResourceInstall() {
		final var pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		final var directory = Mockito.mock(Path.class);
		Mockito.when(pluginsClassLoader.getHomeDirectory()).thenReturn(Paths.get(USER_HOME_DIRECTORY));
		Mockito.when(directory.resolve(ArgumentMatchers.anyString()))
				.thenReturn(Paths
						.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR)
						.resolve("plugin-iam-node-test.jar"));
		Mockito.when(pluginsClassLoader.getPluginDirectory()).thenReturn(directory);
		final var pluginResource = new SystemPluginResource() {
			@Override
			protected PluginsClassLoader getPluginClassLoader() {
				return pluginsClassLoader;
			}

		};
		applicationContext.getAutowireCapableBeanFactory().autowireBean(pluginResource);
		return pluginResource;
	}

	private void doPluginResourceDelete(final String artifact) throws IOException {
		final var pluginResource = newPluginResourceDelete();
		pluginResource.delete(artifact);
	}

	private void doPluginResourceDelete(final String artifact, final String version)
			throws IOException {
		final var pluginResource = newPluginResourceDelete();
		pluginResource.delete(artifact, version);
	}

	private SystemPluginResource newPluginResourceDelete() {
		final var pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		try (var scope = new ThreadClassLoaderScope(new URLClassLoader(new URL[0], pluginsClassLoader))) {
			Assertions.assertNotNull(PluginsClassLoader.getInstance());
			Mockito.when(pluginsClassLoader.getPluginDirectory()).thenReturn(
					Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR));
			final var resource = new SystemPluginResource() {
				@Override
				protected PluginsClassLoader getPluginClassLoader() {
					return pluginsClassLoader;
				}

			};
			applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
			return resource;
		}
	}

	@AfterEach
	void cleanArtifacts() {
		FileUtils.deleteQuietly(TEMP_FILE);
	}

	/**
	 * Remove a non existing plugin : no error
	 */
	@Test
	void removeNotExists() throws IOException {
		doPluginResourceDelete("any");
	}

	/**
	 * Remove a plug-in having explicit dependencies (by name) plug-ins : all related plug-ins are deleted. Note this
	 * feature works only for plug-ins that are not loaded in the classloader. Need an {@link URLClassLoader#close()}
	 */
	@Test
	void removeWidest() throws IOException {
		Assertions.assertFalse(TEMP_FILE.exists());
		FileUtils.touch(TEMP_FILE);
		Assertions.assertTrue(TEMP_FILE.exists());
		doPluginResourceDelete("plugin-iam");
		Assertions.assertFalse(TEMP_FILE.exists());
	}

	/**
	 * Remove the exact plug-in, and only it.
	 */
	@Test
	void removeExact() throws IOException {
		Assertions.assertFalse(TEMP_FILE.exists());
		FileUtils.touch(TEMP_FILE);
		Assertions.assertTrue(TEMP_FILE.exists());
		doPluginResourceDelete("plugin-iam");
		Assertions.assertFalse(TEMP_FILE.exists());
	}

	/**
	 * Remove the exact plug-in + version, and only it.
	 */
	@Test
	void removeExactVersion() throws IOException {
		Assertions.assertFalse(TEMP_FILE.exists());
		FileUtils.touch(TEMP_FILE);
		Assertions.assertTrue(TEMP_FILE.exists());
		doPluginResourceDelete("plugin-iam-node", "test");
		Assertions.assertFalse(TEMP_FILE.exists());
	}

	@Test
	void searchPluginsInMavenRepoNoResult() throws IOException {
		final var result = searchPluginsInMavenRepo("no-result");
		Assertions.assertTrue(result.isEmpty(), "Search result should be empty.");
	}

	@Test
	void searchPluginsOnMavenRepoOneResult() throws IOException {
		final var result = searchPluginsInMavenRepo("samp");
		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals("plugin-sample", result.get(0).getArtifact());
		Assertions.assertEquals("0.0.1", result.get(0).getVersion());
	}

	private List<Artifact> searchPluginsInMavenRepo(final String query) throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();
		return resource.search(query, "central");
	}

	@Test
	void invalidateLastPluginVersions() throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();
		final var versions = centralRepositoryManager.getLastPluginVersions();
		Assertions.assertEquals(versions.keySet(), centralRepositoryManager.getLastPluginVersions().keySet());
		resource.invalidateLastPluginVersions("central");
		Assertions.assertEquals(versions.keySet(), centralRepositoryManager.getLastPluginVersions().keySet());
	}

	@Test
	void persistAsNeeded() {
		final var role = new SystemRole();
		role.setName("any");
		em.persist(role);
		final var user = new SystemUser();
		user.setLogin("any");
		em.persist(user);
		final var entity = new SystemRoleAssignment();
		entity.setRole(role);
		entity.setUser(user);
		resource.persistAsNeeded(SystemRoleAssignment.class, entity);
		em.flush();
		Assertions.assertFalse(entity.isNew());
	}

	@Test
	void persistAsNeededNamed() {
		final var project = new SystemUserSetting();
		project.setName("foo");
		project.setLogin("any");
		project.setValue("v");
		resource.persistAsNeeded(SystemUserSetting.class, project);
		final var project1 = em.find(SystemUserSetting.class, project.getId());
		Assertions.assertNotNull(project1);

		final var project2 = new SystemUserSetting();
		project2.setName("foo");
		project.setLogin("any");
		project.setValue("v");
		resource.persistAsNeeded(SystemUserSetting.class, project2);

		// project2 has not been persisted, duplicate has been prevented
		Assertions.assertTrue(project2.isNew());
	}

	@Test
	void persistAsBusinessEntity() {
		final var user = new SampleBusinessEntity();
		user.setId("foo");
		resource.persistAsNeeded(SampleBusinessEntity.class, user);
		final var user1 = em.find(SampleBusinessEntity.class, "foo");
		Assertions.assertNotNull(user1);

		final var user2 = new SampleBusinessEntity();
		user2.setId("foo");
		resource.persistAsNeeded(SampleBusinessEntity.class, user2);

		// user2 has not been persisted, duplicate has been prevented
		Assertions.assertSame(user1, em.find(SampleBusinessEntity.class, "foo"));
	}

	@Test
	void decorate() {
		final var settings = Mockito.mock(SessionSettings.class);
		Mockito.when(settings.getApplicationSettings()).thenReturn(applicationSettings);
		Assertions.assertNull(applicationSettings.getPlugins());
		resource.decorate(settings);
		Assertions.assertNotNull(settings.getApplicationSettings().getPlugins());
		resource.decorate(settings);
		Assertions.assertNotNull(settings.getApplicationSettings().getPlugins());
	}
}
