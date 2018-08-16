/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
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
import org.ligoj.bootstrap.model.system.SystemBench;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.ligoj.bootstrap.model.system.SystemPlugin;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.ligoj.bootstrap.resource.system.plugin.repository.Artifact;
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

import com.google.common.io.Files;

/**
 * Test class of {@link SystemPluginResource}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class SystemPluginResourceTest extends org.ligoj.bootstrap.AbstractServerTest {

	protected static final String USER_HOME_DIRECTORY = "target/test-classes/home-test";

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
	private ConfigurationResource configuration;

	@BeforeEach
	public void prepareData() throws IOException {
		FileUtils.deleteQuietly(TEMP_FILE);
		configuration.put("ligoj.plugin.ignore", " plugin-sample-ignore , any");
		persistEntities("csv-test", SystemConfiguration.class);
		clearAllCache();
		resource = mockCentral("search.json");
	}

	@AfterEach
	public void cleanIngleton() {
		destroySingleton("restartEndpoint");
	}

	@Test
	public void findAllCentralOnline() throws IOException {
		configuration.delete("plugins.repository-manager.central.search.url");
		Assertions.assertTrue("1.0.0".compareTo(resource.getRepositoryManager("central").getLastPluginVersions()
				.get("plugin-iam-node").getVersion()) <= 0);
	}

	@Test
	public void getRepositoryManager() throws IOException {
		Assertions.assertTrue(resource.getRepositoryManager("not-exist").getLastPluginVersions().isEmpty());
		Assertions.assertNull(resource.getRepositoryManager("not-exist").getArtifactInputStream("any", "1.2.3"));
		resource.getRepositoryManager("not-exist").invalidateLastPluginVersions();
		Assertions.assertEquals("empty", resource.getRepositoryManager("not-exist").getId());
	}

	@Test
	public void findAllNewVersion() throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();
		final PluginListener listener = Mockito.mock(PluginListener.class);
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
	public void findAllInstalledNextSameVersion() throws IOException {
		final SystemPluginResource resource = mockCentral("search.json");
		final String currentVersion = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-foo".equals(p.getPlugin().getArtifact())).findFirst().get().getPlugin()
				.getVersion();
		resource.getPluginClassLoader().getInstalledPlugins().put("plugin-foo",
				"plugin-foo-" + PluginsClassLoader.toExtendedVersion(currentVersion));

		final PluginVo pluginVo = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-foo".equals(p.getPlugin().getArtifact())).findFirst().get();
		Assertions.assertNotNull(pluginVo.getLatestLocalVersion());
		Assertions.assertEquals(currentVersion, pluginVo.getPlugin().getVersion());
		Assertions.assertFalse(pluginVo.isDeleted());
		Assertions.assertNull(pluginVo.getNewVersion());
		Assertions.assertEquals("Foo", pluginVo.getName());
	}

	@Test
	public void findAllInstalledNextNewPlugin() throws IOException {
		final SystemPluginResource resource = mockCentral("search.json");
		resource.getPluginClassLoader().getInstalledPlugins().put("plugin-sample",
				"plugin-sample-Z0000001Z0000002Z0000003Z0000004");

		final PluginVo pluginVo = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-sample".equals(p.getPlugin().getArtifact())).findFirst().get();
		Assertions.assertEquals("1.2.3.4", pluginVo.getLatestLocalVersion());
		Assertions.assertNull(pluginVo.getPlugin().getVersion());
		Assertions.assertFalse(pluginVo.isDeleted());
		Assertions.assertNull(pluginVo.getNewVersion());
		Assertions.assertEquals("plugin-sample", pluginVo.getName());
	}

	@Test
	public void findAllInstalledNextUpdate() throws IOException {
		final SystemPluginResource resource = mockCentral("search.json");
		resource.getPluginClassLoader().getInstalledPlugins().put("plugin-bar",
				"plugin-bar-Z0000123Z0000004Z0000005Z0000000");

		final PluginVo pluginVo = filter(resource.findAll("central")).stream()
				.filter(p -> "plugin-bar".equals(p.getPlugin().getArtifact())).findFirst().get();
		Assertions.assertEquals("123.4.5", pluginVo.getLatestLocalVersion());
		Assertions.assertNull(pluginVo.getPlugin().getVersion());
		Assertions.assertNull(pluginVo.getNewVersion());
		Assertions.assertFalse(pluginVo.isDeleted());
		Assertions.assertEquals("plugin-bar", pluginVo.getName());
	}

	@Test
	public void findAllSameVersion() throws IOException {
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
	public void isDeleted() {
		final PluginVo plugin = new PluginVo();
		plugin.setLocation("any");
		Assertions.assertTrue(resource.isDeleted(plugin));
	}

	@Test
	public void isDeletedExist() {
		final PluginVo plugin = new PluginVo();
		plugin.setLocation(USER_HOME_DIRECTORY);
		Assertions.assertFalse(resource.isDeleted(plugin));
	}

	@Test
	public void toTrimmedVersion() {
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
	public void autoUpdateNoPluginMatch() throws IOException {
		Assertions.assertEquals(0, mockCentral("search.json").autoUpdate());
	}

	@Test
	public void autoUpdateNoNewVersion() throws IOException {
		Assertions.assertEquals(0, mockCentral("search-foo.json").autoUpdate());
	}

	@Test
	public void autoUpdateNewVersion() throws IOException {
		Assertions.assertEquals(1, mockCentral("search-bar.json").autoUpdate());
	}

	private SystemPluginResource mockCentral(final String body) throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/" + body).getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		final PluginsClassLoader pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		Mockito.when(pluginsClassLoader.getHomeDirectory())
				.thenReturn(Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER));
		Mockito.when(pluginsClassLoader.getPluginDirectory()).thenReturn(
				Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR));
		final Map<String, String> map = new HashMap<>();
		map.put("plugin-foo", "plugin-foo-Z0000001Z0000000Z0000001Z0000000");
		map.put("plugin-bar", "plugin-bar-Z0000001Z0000000Z0000000Z0000000");
		Mockito.when(pluginsClassLoader.getInstalledPlugins()).thenReturn(map);
		final SystemPluginResource pluginResource = new SystemPluginResource() {
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
		final SystemPlugin pluginId = new SystemPlugin();
		pluginId.setVersion(version);
		pluginId.setKey("service:sample");
		pluginId.setType("SERVICE");
		pluginId.setArtifact("plugin-sample");
		repository.saveAndFlush(pluginId);

		final List<PluginVo> plugins = filter(resource.findAll("central"));
		Assertions.assertEquals(3, plugins.size()); // "foo", "bar", "Sample"

		// External plug-in service
		final PluginVo plugin2 = plugins.get(2);
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
		return plugins.stream().filter(p -> !"feature:ui".equals(p.getId())).collect(Collectors.toList());
	}

	@Test
	public void findAllOrphan() throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		try (ThreadClassLoaderScope scope = new ThreadClassLoaderScope(new URLClassLoader(
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
	public void findAllJustInstalled() throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();

		try (ThreadClassLoaderScope scope = new ThreadClassLoaderScope(new URLClassLoader(
				new URL[] { Thread.currentThread().getContextClassLoader()
						.getResource("home-test/.ligoj/plugins/plugin-bar-1.0.0.jar") },
				Thread.currentThread().getContextClassLoader()))) {

			// Register a feature from a JAR
			registerSingleton("barResource",
					scope.getScopedClassLoader().loadClass("org.ligoj.app.plugin.bar.BarResource").getConstructors()[0]
							.newInstance());
			final SystemPlugin plugin = new SystemPlugin();
			plugin.setKey("feature:bar");
			plugin.setVersion("1.0.0");
			plugin.setArtifact("plugin-bar");
			plugin.setType("FEATURE");
			repository.saveAndFlush(plugin);

			final List<PluginVo> plugins = filter(resource.findAll("central"));
			Assertions.assertEquals(2, plugins.size());

			// Plug-in from the API
			final PluginVo plugin0 = plugins.get(0);
			Assertions.assertEquals("feature:bar", plugin0.getId());
			Assertions.assertEquals("BAR", plugin0.getName());
			Assertions.assertEquals("COMPANY", plugin0.getVendor());
			Assertions.assertNull(plugin0.getNewVersion());
			Assertions.assertTrue(plugin0.getLocation().endsWith(".jar"));
			Assertions.assertEquals("1.0.0", plugin0.getPlugin().getVersion());
			Assertions.assertEquals("FEATURE", plugin0.getPlugin().getType());

			// Plug-in (feature) embedded in the current project
			final PluginVo plugin1 = plugins.get(1);
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
		final SystemPlugin orphanPLugin = new SystemPlugin();
		orphanPLugin.setKey("any");
		orphanPLugin.setArtifact("plugin-any");
		orphanPLugin.setType("TOOL");
		orphanPLugin.setVersion("1.1");
		repository.saveAndFlush(orphanPLugin);

		final List<PluginVo> plugins = filter(resource.findAll("central"));
		Assertions.assertEquals(2, plugins.size());

		// Plug-in in the classpath
		final PluginVo plugin0 = plugins.get(0);
		Assertions.assertEquals("feature:foo", plugin0.getId());
		Assertions.assertEquals("Foo", plugin0.getName());
		Assertions.assertNull(plugin0.getVendor());
		Assertions.assertNull(plugin0.getNewVersion());
		Assertions.assertFalse(plugin0.getLocation().endsWith(".jar"));
		Assertions.assertNotNull(plugin0.getPlugin().getVersion());
		Assertions.assertEquals("FEATURE", plugin0.getPlugin().getType());

		// Plug-in present in the plug-ins directory but not in the class-path
		final PluginVo plugin1 = plugins.get(1);
		Assertions.assertEquals("plugin-bar", plugin1.getId());
		Assertions.assertEquals("plugin-bar", plugin1.getName());
		Assertions.assertNull(plugin1.getVendor());
		Assertions.assertNull(plugin1.getLocation());
		Assertions.assertNull(plugin1.getPlugin().getVersion());
		Assertions.assertNull(plugin1.getPlugin().getType());
	}

	@Test
	public void configurePluginInstall() {
		// The class-loader of this mock corresponds to the one related to SampleService
		// So corresponds to API jar and not this project
		final SampleService service1 = new SampleService();
		resource.configurePluginInstall(service1);
		registerSingleton("mockPluginListener", new MockPluginListener());
		try {
			Assertions.assertEquals("FEATURE", repository.findByExpected("key", "service:sample").getType());
			Assertions.assertNotNull(repository.findByExpected("key", "service:sample").getVersion());

			final SampleTool1 service2 = new SampleTool1();
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
	public void configurePluginInstallManagedEntitiesNotSameClassloader() {
		final FeaturePlugin service1 = Mockito.mock(FeaturePlugin.class);
		Mockito.when(service1.getKey()).thenReturn("service:sample");
		Mockito.when(service1.getInstalledEntities()).thenReturn(Arrays.asList(SystemBench.class));
		Assertions.assertThrows(TechnicalException.class, () -> {
			resource.configurePluginInstall(service1);
		});
	}

	@Test
	public void configurePluginInstallError() {
		final FeaturePlugin service1 = Mockito.mock(FeaturePlugin.class);
		Mockito.when(service1.getInstalledEntities()).thenThrow(ValidationJsonException.class);
		Assertions.assertThrows(TechnicalException.class, () -> {
			resource.configurePluginInstall(service1);
		});
	}

	@Test
	public void manifestData() {
		Assertions.assertTrue(Integer.class.getModule().getDescriptor().rawVersion().get().matches("\\d+\\.\\d+\\..*"));
		Assertions.assertEquals("java.base", Integer.class.getModule().getName());
	}

	@Test
	public void configurePluginUpdate() {
		final SampleService service1 = new SampleService();
		final SystemPlugin plugin = new SystemPlugin();
		plugin.setVersion("old version");
		resource.configurePluginUpdate(service1, plugin);
		Assertions.assertNotNull("1.0", plugin.getVersion());
	}

	@Test
	public void getVersion() {
		// Version is resolved from the date
		Assertions.assertTrue(resource.getVersion(new SampleService()).startsWith("20"));
	}

	@Test
	public void getVersionIOException() {
		Assertions.assertEquals("?", new SystemPluginResource() {
			@Override
			protected String getLastModifiedTime(final FeaturePlugin plugin) throws IOException, URISyntaxException {
				throw new IOException();
			}
		}.getVersion(Mockito.mock(FeaturePlugin.class)));
	}

	@Test
	public void getVersionURISyntaxException() {
		Assertions.assertEquals("?", new SystemPluginResource() {
			@Override
			protected String getLastModifiedTime(final FeaturePlugin plugin) throws IOException, URISyntaxException {
				throw new URISyntaxException("input", "reason");
			}
		}.getVersion(Mockito.mock(FeaturePlugin.class)));
	}

	@Test
	public void refreshPlugins() throws Exception {
		final ContextRefreshedEvent event = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		registerSingleton("mockPluginListener", new MockPluginListener());
		try {
			resource.refreshPlugins(event);
		} finally {
			destroySingleton("mockPluginListener");
		}
	}

	@Test
	public void refreshPluginsVetoInstall() throws Exception {
		final ContextRefreshedEvent event = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(event.getApplicationContext()).thenReturn(applicationContext);
		PluginListener listener = Mockito.mock(PluginListener.class);
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
	public void refreshPluginsAutoUpdate() throws Exception {
		configuration.put("ligoj.plugin.update", "true");
		final AtomicBoolean check = new AtomicBoolean(false);
		final SystemPluginResource resource = new SystemPluginResource() {
			@Override
			public int autoUpdate() throws IOException {
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
	public void refreshPluginsAutoUpdateNoUpdate() throws Exception {
		configuration.put("ligoj.plugin.update", "true");
		final AtomicBoolean check = new AtomicBoolean(false);
		final SystemPluginResource resource = new SystemPluginResource() {
			@Override
			public int autoUpdate() throws IOException {
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
	public void refreshPluginsUpdate() throws Exception {
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

			final ContextRefreshedEvent event = Mockito.mock(ContextRefreshedEvent.class);
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
	public void configurePluginEntityNotFound() {
		Assertions.assertThrows(TechnicalException.class, () -> {
			new SystemPluginResource().configurePluginEntity(Arrays.stream(new URL[] { new URL("file://tmp") }),
					SystemUser.class, "---");
		});
	}

	@Test
	public void configurePluginEntityFromJar() throws MalformedURLException, IOException {
		try (ThreadClassLoaderScope scope = new ThreadClassLoaderScope(new URLClassLoader(
				new URL[] { Thread.currentThread().getContextClassLoader()
						.getResource("home-test/.ligoj/plugins/plugin-bar-1.0.0.jar") },
				Thread.currentThread().getContextClassLoader()))) {
			final URL url = Thread.currentThread().getContextClassLoader()
					.getResource("csv/sample-business-entity.csv");
			final SystemPluginResource pluginResource = new SystemPluginResource();
			pluginResource.em = Mockito.mock(EntityManager.class);
			pluginResource.csvForJpa = Mockito.mock(CsvForJpa.class);
			pluginResource.configurePluginEntity(Arrays.stream(new URL[] { url }), SampleBusinessEntity.class,
					url.getPath());
		}
	}

	@Test
	public void configurePluginEntityFromProject() throws MalformedURLException, IOException {
		final URL url = Thread.currentThread().getContextClassLoader()
				.getResource("csv-test/sample-business-entity.csv");
		final SystemPluginResource pluginResource = new SystemPluginResource();
		pluginResource.em = Mockito.mock(EntityManager.class);
		pluginResource.csvForJpa = csvForJpa;
		pluginResource.configurePluginEntity(Arrays.stream(new URL[] { url }), SampleBusinessEntity.class,
				url.toString());
	}

	@Test
	public void installNotExists() {
		Assertions.assertThrows(ValidationJsonException.class, () -> {
			newPluginResourceInstall().install("any", "central");
		});
	}

	@Test
	public void installNotExistsVersion() {
		MatcherUtil.assertThrows(Assertions.assertThrows(ValidationJsonException.class, () -> {
			newPluginResourceInstall().install("any", "dummy", "central");
		}), "artifact", "Cannot be installed");
	}

	@Test
	public void installCentralOnline() throws IOException {
		configuration.delete("plugins.repository-manager.central.artifact.url");
		configuration.delete("plugins.repository-manager.central.search.url");
		newPluginResourceInstall().install("plugin-iam-node", "central");
		Assertions.assertTrue(TEMP_FILE.exists());
	}

	@Test
	public void upload() throws IOException {
		final InputStream input = new ByteArrayInputStream("test".getBytes("UTF-8"));
		newPluginResourceInstall().upload(input, "plugin-sample", "1.2.9");
		Assertions.assertTrue(TEMP_FILE.exists());
		Assertions.assertEquals("test", FileUtils.readFileToString(TEMP_FILE, "UTF-8"));
	}

	@Test
	public void installCentral() throws IOException {
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
	public void installNexusOnline() throws IOException {
		configuration.delete("plugins.repository-manager.nexus.artifact.url");
		configuration.delete("plugins.repository-manager.nexus.search.url");
		newPluginResourceInstall().install("plugin-iam-node", "nexus");
		Assertions.assertTrue(TEMP_FILE.exists());
	}

	@Test
	public void installNexus() throws IOException {
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
	public void getPluginClassLoaderOutOfClassLoader() {
		Assertions.assertNull(new SystemPluginResource().getPluginClassLoader());
	}

	@Test
	public void restart() throws InterruptedException {
		// Difficult to test...
		resource.restart();
		Thread.sleep(100);
		Mockito.verify(restartEndpoint).restart();
	}

	@Test
	public void getPluginClassLoader() {
		final PluginsClassLoader pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		try (ThreadClassLoaderScope scope = new ThreadClassLoaderScope(
				new URLClassLoader(new URL[0], pluginsClassLoader))) {
			Assertions.assertNotNull(resource.getPluginClassLoader());
		}
	}

	private SystemPluginResource newPluginResourceInstall() {
		final PluginsClassLoader pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		final Path directory = Mockito.mock(Path.class);
		Mockito.when(pluginsClassLoader.getHomeDirectory()).thenReturn(Paths.get(USER_HOME_DIRECTORY));
		Mockito.when(directory.resolve(ArgumentMatchers.anyString()))
				.thenReturn(Paths
						.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR)
						.resolve("plugin-iam-node-test.jar"));
		Mockito.when(pluginsClassLoader.getPluginDirectory()).thenReturn(directory);
		final SystemPluginResource pluginResource = new SystemPluginResource() {
			@Override
			protected PluginsClassLoader getPluginClassLoader() {
				return pluginsClassLoader;
			}

		};
		applicationContext.getAutowireCapableBeanFactory().autowireBean(pluginResource);
		return pluginResource;
	}

	private SystemPluginResource newPluginResourceDelete(final String artifact) throws IOException {
		final SystemPluginResource pluginResource = newPluginResourceDelete();
		pluginResource.delete(artifact);
		return pluginResource;
	}

	private SystemPluginResource newPluginResourceDelete(final String artifact, final String version)
			throws IOException {
		final SystemPluginResource pluginResource = newPluginResourceDelete();
		pluginResource.delete(artifact, version);
		return pluginResource;
	}

	private SystemPluginResource newPluginResourceDelete() {
		final PluginsClassLoader pluginsClassLoader = Mockito.mock(PluginsClassLoader.class);
		try (ThreadClassLoaderScope scope = new ThreadClassLoaderScope(
				new URLClassLoader(new URL[0], pluginsClassLoader))) {
			Assertions.assertNotNull(PluginsClassLoader.getInstance());
			Mockito.when(pluginsClassLoader.getPluginDirectory()).thenReturn(
					Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR));
			final SystemPluginResource resource = new SystemPluginResource() {
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
	public void cleanArtifacts() {
		FileUtils.deleteQuietly(TEMP_FILE);
	}

	/**
	 * Remove a non existing plugin : no error
	 */
	@Test
	public void removeNotExists() throws IOException {
		newPluginResourceDelete("any");
	}

	/**
	 * Remove a plug-in having explicit depending (by name) plug-ins : all related plug-ins are deleted. Note this
	 * feature works only for plug-ins that are not loaded in the classloader. Need an {@link URLClassLoader#close()}
	 */
	@Test
	public void removeWidest() throws IOException {
		Assertions.assertFalse(TEMP_FILE.exists());
		Files.touch(TEMP_FILE);
		Assertions.assertTrue(TEMP_FILE.exists());
		newPluginResourceDelete("plugin-iam");
		Assertions.assertFalse(TEMP_FILE.exists());
	}

	/**
	 * Remove the exact plug-in, and only it.
	 */
	@Test
	public void removeExact() throws IOException {
		Assertions.assertFalse(TEMP_FILE.exists());
		Files.touch(TEMP_FILE);
		Assertions.assertTrue(TEMP_FILE.exists());
		newPluginResourceDelete("plugin-iam");
		Assertions.assertFalse(TEMP_FILE.exists());
	}

	/**
	 * Remove the exact plug-in + version, and only it.
	 */
	@Test
	public void removeExactVersion() throws IOException {
		Assertions.assertFalse(TEMP_FILE.exists());
		Files.touch(TEMP_FILE);
		Assertions.assertTrue(TEMP_FILE.exists());
		newPluginResourceDelete("plugin-iam-node", "test");
		Assertions.assertFalse(TEMP_FILE.exists());
	}

	@Test
	public void searchPluginsInMavenRepoNoResult() throws IOException {
		final List<Artifact> result = searchPluginsInMavenRepo("no-result");
		Assertions.assertTrue(result.isEmpty(), "Search result should be empty.");
	}

	@Test
	public void searchPluginsOnMavenRepoOneResult() throws IOException {
		final List<Artifact> result = searchPluginsInMavenRepo("samp");
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
	public void invalidateLastPluginVersions() throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();
		final Map<String, Artifact> versions = centralRepositoryManager.getLastPluginVersions();
		Assertions.assertEquals(versions.keySet(), centralRepositoryManager.getLastPluginVersions().keySet());
		resource.invalidateLastPluginVersions("central");
		Assertions.assertEquals(versions.keySet(), centralRepositoryManager.getLastPluginVersions().keySet());
	}

	@Test
	public void persistAsNeeded() {
		final SystemRole role = new SystemRole();
		role.setName("any");
		em.persist(role);
		final SystemUser user = new SystemUser();
		user.setLogin("any");
		em.persist(user);
		final SystemRoleAssignment entity = new SystemRoleAssignment();
		entity.setRole(role);
		entity.setUser(user);
		resource.persistAsNeeded(SystemRoleAssignment.class, entity);
		em.flush();
		Assertions.assertFalse(entity.isNew());
	}

	@Test
	public void persistAsNeededNamed() {
		final SystemUserSetting project = new SystemUserSetting();
		project.setName("foo");
		project.setLogin("any");
		project.setValue("v");
		resource.persistAsNeeded(SystemUserSetting.class, project);
		final SystemUserSetting project1 = em.find(SystemUserSetting.class, project.getId());
		Assertions.assertNotNull(project1);

		final SystemUserSetting project2 = new SystemUserSetting();
		project2.setName("foo");
		project.setLogin("any");
		project.setValue("v");
		resource.persistAsNeeded(SystemUserSetting.class, project2);

		// project2 has not been persisted, duplicate has been prevented
		Assertions.assertTrue(project2.isNew());
	}

	@Test
	public void persistAsBusinessEntity() {
		final SampleBusinessEntity user = new SampleBusinessEntity();
		user.setId("foo");
		resource.persistAsNeeded(SampleBusinessEntity.class, user);
		final SampleBusinessEntity user1 = em.find(SampleBusinessEntity.class, "foo");
		Assertions.assertNotNull(user1);

		final SampleBusinessEntity user2 = new SampleBusinessEntity();
		user2.setId("foo");
		resource.persistAsNeeded(SampleBusinessEntity.class, user2);

		// user2 has not been persisted, duplicate has been prevented
		Assertions.assertSame(user1, em.find(SampleBusinessEntity.class, "foo"));
	}

}
