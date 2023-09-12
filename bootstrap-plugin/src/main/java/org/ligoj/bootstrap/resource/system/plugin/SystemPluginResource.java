/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin;

import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.ligoj.bootstrap.core.INamableBean;
import org.ligoj.bootstrap.core.NamedBean;
import org.ligoj.bootstrap.core.dao.csv.CsvForJpa;
import org.ligoj.bootstrap.core.model.AbstractBusinessEntity;
import org.ligoj.bootstrap.core.plugin.FeaturePlugin;
import org.ligoj.bootstrap.core.plugin.PluginListener;
import org.ligoj.bootstrap.core.plugin.PluginVo;
import org.ligoj.bootstrap.core.plugin.PluginsClassLoader;
import org.ligoj.bootstrap.core.resource.TechnicalException;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.ligoj.bootstrap.dao.system.SystemPluginRepository;
import org.ligoj.bootstrap.model.system.SystemPlugin;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.ligoj.bootstrap.resource.system.plugin.repository.Artifact;
import org.ligoj.bootstrap.resource.system.plugin.repository.EmptyRepositoryManager;
import org.ligoj.bootstrap.resource.system.plugin.repository.RepositoryManager;
import org.ligoj.bootstrap.resource.system.session.ISessionSettingsProvider;
import org.ligoj.bootstrap.resource.system.session.SessionSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manage plug-in life-cycle.
 *
 * @see <a href="https://repository.sonatype.org/nexus-indexer-lucene-plugin/default/docs/path__lucene_search.html">OSS
 * lucene_search</a>
 */
@Path("/system/plugin")
@Slf4j
@Component
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class SystemPluginResource implements ISessionSettingsProvider {

	private static final String REPO_CENTRAL = "central";

	/**
	 * Property identifying an array of plug-ins to ignore.
	 */
	private static final String PLUGIN_IGNORE = "ligoj.plugin.ignore";

	/**
	 * Plug-ins auto install flag.
	 */
	private static final String PLUGIN_INSTALL = "ligoj.plugin.install";

	/**
	 * Plug-ins auto update flag.
	 */
	private static final String PLUGIN_UPDATE = "ligoj.plugin.update";

	/**
	 * Plug-ins repository used for auto-update mode.
	 */
	private static final String PLUGIN_REPOSITORY = "ligoj.plugin.repository";

	private static final RepositoryManager EMPTY_REPOSITORY = new EmptyRepositoryManager();

	@Autowired
	private SystemPluginRepository repository;

	/**
	 * Injected CSV bean mapper for JPA shared with child classes.
	 */
	@Autowired
	protected CsvForJpa csvForJpa;

	/**
	 * Injected entity manager shared with child classes.
	 */
	@Autowired
	protected EntityManager em;

	@Autowired
	private RestartEndpoint restartEndpoint;

	/**
	 * Injected Spring context
	 */
	@Autowired
	protected ApplicationContext context;

	@Autowired
	private ConfigurationResource configuration;

	/**
	 * Return all plug-ins with details from a given repository.
	 *
	 * @param repository The repository identifier to query.
	 * @return All plug-ins with details.
	 * @throws IOException When the last version index file cannot be retrieved.
	 */
	@GET
	public List<PluginVo> findAll(@QueryParam("repository") @DefaultValue(REPO_CENTRAL) final String repository)
			throws IOException {
		// Get the available plug-ins
		Map<String, Artifact> lastVersion = new HashMap<>();
		try {
			lastVersion = getLastPluginVersions(repository);
		} catch (IOException ioe) {
			log.warn("Unable to get latest version from repository {}", repository, ioe);
		}
		final var enabledFeatures = context.getBeansOfType(FeaturePlugin.class);
		final var lastVersionF = lastVersion;

		// Get the enabled plug-in features
		final var enabled = this.repository.findAll().stream()
				.map(p -> toVo(lastVersionF, p,
						enabledFeatures.values().stream().filter(f -> p.getKey().equals(f.getKey())).findFirst()
								.orElse(null)))
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(p -> p.getPlugin().getArtifact(), Function.identity()));

		// Add pending installation: available but not yet enabled plug-ins
		getPluginClassLoader().getInstalledPlugins().forEach((id, v) -> {
			enabled.computeIfPresent(id, (k, p) -> {
				// Check if it's an update
				if (!p.getPlugin().getVersion().equals(toTrimmedVersion(v))) {
					// Corresponds to a different version
					p.setLatestLocalVersion(toTrimmedVersion(v));
				}
				p.setDeleted(isDeleted(p));
				return p;
			});

			// Add new plug-ins
			enabled.computeIfAbsent(id, k -> {
				final var plugin = new SystemPlugin();
				plugin.setArtifact(k);
				plugin.setKey("?:" + Arrays.stream(k.split("-")).skip(1).collect(Collectors.joining("-")));

				final var p = new PluginVo();
				p.setId(k);
				p.setName(k);
				p.setPlugin(plugin);
				p.setLatestLocalVersion(toTrimmedVersion(v));
				return p;
			});
		});

		//
		return enabled.values().stream().sorted(Comparator.comparing(NamedBean::getId)).toList();
	}

	/**
	 * Indicate the plug-in is deleted or not.
	 *
	 * @param plugin The plug-in to check.
	 * @return <code>true</code> when the plug-in is deleted locally from the FS.
	 */
	protected boolean isDeleted(final PluginVo plugin) {
		return !new File(plugin.getLocation()).exists();
	}

	/**
	 * Convert an extended version to a trim one. Example:
	 * <ul>
	 * <li><code>plugin-sample-Z0000001Z0000002Z0000003Z0000004</code> will be <code>1.2.3.4</code></li>
	 * <li><code>plugin-sample-Z0000001Z0000002Z0000003Z0000000</code> will be <code>1.2.3</code></li>
	 * <li><code>plugin-sample-Z0000001Z0000002Z0000003SNAPSHOT</code> will be <code>1.2.3-SNAPSHOT</code></li>
	 * <li><code>plugin-sample-1.2.3.4</code> will be <code>1.2.3.4</code></li>
	 * <li><code>1.2.3.4</code> will be <code>1.2.3.4</code></li>
	 * <li><code>1.2.3.4</code> will be <code>1.2.3.4</code></li>
	 * <li><code>1.2.3.0</code> will be <code>1.2.3</code></li>
	 * <li><code>0.1.2.3</code> will be <code>0.1.2.3</code></li>
	 * </ul>
	 *
	 * @param extendedVersion The extended version. Trim version is also accepted.
	 * @return Trim version.
	 */
	protected String toTrimmedVersion(final String extendedVersion) {
		var trim = Arrays.stream(StringUtils.split(extendedVersion, "-Z.")).dropWhile(s -> !s.matches("^(Z?\\d+.*)"))
				.map(s -> StringUtils.defaultIfBlank(RegExUtils.replaceFirst(s, "^0+", ""), "0"))
				.collect(Collectors.joining(".")).replace(".SNAPSHOT", "-SNAPSHOT")
				.replaceFirst("([^-])SNAPSHOT", "$1-SNAPSHOT");
		if (trim.endsWith(".0") && StringUtils.countMatches(trim, '.') > 2) {
			trim = StringUtils.removeEnd(trim, ".0");
		}
		return trim;
	}

	/**
	 * Build the plug-in information from the plug-in itself and the last version being available.
	 */
	private PluginVo toVo(final Map<String, Artifact> lastVersion, final SystemPlugin p, final FeaturePlugin feature) {
		if (feature == null) {
			// Plug-in is no more available or in fail-safe mode
			return null;
		}

		final var extension = context.getBeansOfType(PluginListener.class).values().stream().findFirst();
		// Plug-in implementation is available
		final var vo = extension.map(PluginListener::toVo).orElse(PluginVo::new).get();
		vo.setId(p.getKey());
		vo.setName(StringUtils.removeStart(feature.getName(), "Ligoj - Plugin "));
		vo.setLocation(getPluginLocation(feature).getPath());
		vo.setVendor(feature.getVendor());
		vo.setPlugin(p);

		// Expose the resolve newer version
		vo.setNewVersion(Optional
				.ofNullable(lastVersion.get(p.getArtifact())).map(Artifact::getVersion).filter(v -> PluginsClassLoader
						.toExtendedVersion(v).compareTo(PluginsClassLoader.toExtendedVersion(p.getVersion())) > 0)
				.orElse(null));

		extension.ifPresent(e -> e.fillVo(p, feature, vo));
		return vo;
	}

	/**
	 * Search plug-ins in repository which can be installed.
	 *
	 * @param query      The optional searched term.
	 * @param repository The repository identifier to query.
	 * @return All plug-ins artifacts name.
	 * @throws IOException When the last version index file cannot be retrieved.
	 */
	@GET
	@Path("search")
	public List<Artifact> search(@QueryParam("q") @DefaultValue("") final String query,
			@QueryParam("repository") @DefaultValue(REPO_CENTRAL) final String repository) throws IOException {
		return getLastPluginVersions(repository).values().stream().filter(a -> a.getArtifact().contains(query))
				.toList();
	}

	/**
	 * Return the {@link RepositoryManager} with the given identifier.
	 *
	 * @param repository The repository identifier.
	 * @return The {@link RepositoryManager} with the given identifier or {@link #EMPTY_REPOSITORY}
	 */
	protected RepositoryManager getRepositoryManager(final String repository) {
		return context.getBeansOfType(RepositoryManager.class).values().stream()
				.filter(r -> r.getId().equals(repository)).findFirst().orElse(EMPTY_REPOSITORY);
	}

	/**
	 * Request a restart of the current application context in a separated thread.
	 */
	@PUT
	@Path("restart")
	public void restart() {
		final var restartThread = new Thread(() -> restartEndpoint.restart(), "Restart"); // NOPMD
		restartThread.setDaemon(false);
		restartThread.start();
	}

	/**
	 * Request a reset of plug-in cache meta-data
	 *
	 * @param repository The repository identifier to reset.
	 */
	@PUT
	@Path("cache")
	public void invalidateLastPluginVersions(
			@QueryParam("repository") @DefaultValue(REPO_CENTRAL) final String repository) {
		getRepositoryManager(repository).invalidateLastPluginVersions();
	}

	/**
	 * Remove all versions the specified plug-in and the related (by name) plug-ins.
	 *
	 * @param artifact The Maven artifact identifier and also corresponding to the plug-in simple name.
	 * @throws IOException When the file cannot be read or deleted from the file system.
	 */
	@DELETE
	@Path("{artifact:[\\w-]+}")
	public void delete(@PathParam("artifact") final String artifact) throws IOException {
		removeFilter(artifact, "(-.*)?");
		log.info("Plugin {} has been deleted, restart is required", artifact);
	}

	/**
	 * Remove the specific version of a plug-in.
	 *
	 * @param artifact The Maven artifact identifier and also corresponding to the plug-in simple name.
	 * @param version  The specific version.
	 * @throws IOException When the file cannot be read or deleted from the file system.
	 */
	@DELETE
	@Path("{artifact:[\\w-]+}/{version}")
	public void delete(@PathParam("artifact") final String artifact, @PathParam("version") final String version)
			throws IOException {
		removeFilter(artifact, "-" + version.replace(".", "\\."));
		log.info("Plugin {} v{} has been deleted, restart is required", artifact, version);
	}

	private void removeFilter(final String artifact, final String filter) throws IOException {
		try (var list = Files.list(getPluginClassLoader().getPluginDirectory())) {
			list.filter(p -> p.getFileName().toString().matches("^" + artifact + filter + "\\.jar$"))
					.forEach(p -> p.toFile().delete());
		}
	}

	/**
	 * Upload a file of entries to create or update users. The whole entry is replaced.
	 *
	 * @param input    The Maven artifact file.
	 * @param pluginId The Maven <code>artifactId</code>.
	 * @param version  The Maven <code>version</code>.
	 */
	@PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("upload")
	public void upload(@Multipart(value = "plugin-file") final InputStream input,
			@Multipart(value = "plugin-id") final String pluginId,
			@Multipart(value = "plugin-version") final String version) {
		install(input, pluginId, version, "(local)");
	}

	/**
	 * Install or update to the last available version of given plug-in from the remote server.
	 *
	 * @param artifact   The Maven artifact identifier and also corresponding to the plug-in simple name.
	 * @param repository The repository identifier to query.
	 * @throws IOException When install failed.
	 */
	@POST
	@Path("{artifact:[\\w-]+}")
	public void install(@PathParam("artifact") final String artifact,
			@QueryParam("repository") @DefaultValue(REPO_CENTRAL) final String repository) throws IOException {
		final var resultItem = getLastPluginVersions(repository).get(artifact);
		if (resultItem == null) {
			// Plug-in not found, or not the last version
			throw new ValidationJsonException("artifact",
					String.format("No latest version on repository %s", repository));
		}
		install(artifact, resultItem.getVersion(), repository);
	}

	/**
	 * Install the specific version of given plug-in from the remote server. The previous version is not deleted. The
	 * downloaded version will be used only if it is a most recent version than the locally ones.
	 *
	 * @param artifact   The Maven artifact identifier and also corresponding to the plug-in simple name.
	 * @param version    The version to install.
	 * @param repository The repository identifier to query.
	 */
	@POST
	@Path("{artifact:[\\w-]+}/{version:[\\w-]+}")
	public void install(@PathParam("artifact") final String artifact, @PathParam("version") final String version,
			@QueryParam("repository") @DefaultValue(REPO_CENTRAL) final String repository) {
		install(null, artifact, version, repository);
	}

	private void install(final InputStream input, final String artifact, final String version,
			final String repository) {
		final var classLoader = getPluginClassLoader();
		final var target = classLoader.getPluginDirectory().resolve(artifact + "-" + version + ".jar");
		log.info("Download plug-in {} v{} from {}", artifact, version, repository);
		try {
			// Get the right input
			final var input2 = input == null
					? getRepositoryManager(repository).getArtifactInputStream(artifact, version)
					: input;
			// Download and copy the file, note the previous version is not removed
			Files.copy(input2, target, StandardCopyOption.REPLACE_EXISTING);
			log.info("Plugin {} v{} has been installed, restart is required", artifact, version);
		} catch (final Exception ioe) {
			// Installation failed, either download, either FS error
			log.info("Unable to install plugin {} v{} from {}", artifact, version, repository, ioe);
			throw new ValidationJsonException("artifact", "cannot-be-installed", "id", artifact);
		}
	}

	private Map<String, Artifact> getLastPluginVersions(final String repository) throws IOException {
		final var versions = getRepositoryManager(repository).getLastPluginVersions();

		// Remove ignored plug-ins
		Arrays.stream(configuration.get(PLUGIN_IGNORE, "").split(",")).map(String::trim).forEach(versions::remove);
		return versions;
	}

	/**
	 * Return the current plug-in class loader.
	 *
	 * @return The current plug-in class loader.
	 */
	protected PluginsClassLoader getPluginClassLoader() {
		return PluginsClassLoader.getInstance();
	}

	/**
	 * Handle the newly installed plug-ins implementing {@link FeaturePlugin}. Note the plug-ins are installed in a
	 * natural order based on their key's name to ensure the parents plug-ins are configured first. <br>
	 * Note the transactional behavior of this process : if one plug-in failed to be configured, then the entire process
	 * is cancelled. The previously plug-ins and the not processed discovered one are not configured.
	 *
	 * @param event The Spring event.
	 * @throws Exception When the context can not be refreshed because of plug-in updates or configurations.
	 */
	@EventListener
	public void refreshPlugins(final ContextRefreshedEvent event) throws Exception {
		// Auto install plug-ins
		final var install = Arrays.stream(configuration.get(PLUGIN_INSTALL, "").split(",")).map(StringUtils::trimToNull)
				.filter(Objects::nonNull).collect(Collectors.toSet());
		var counter = install.isEmpty() ? 0 : autoInstall(install);

		// Auto update plug-ins
		if (BooleanUtils.toBoolean(configuration.get(PLUGIN_UPDATE, "false"))) {
			// Update the plug-ins
			counter += autoUpdate();
		}
		if (counter > 0) {
			log.info("{} plug-ins have been updated/installed, context will be restarted", counter);
			restart();
			return;
		}
		log.info("No plug-ins have been automatically downloaded for update");

		refreshPlugins(event.getApplicationContext());
	}

	private void refreshPlugins(final ApplicationContext context) throws Exception {
		// Get the existing plug-in features
		final var plugins = repository.findAll().stream()
				.collect(Collectors.toMap(SystemPlugin::getKey, Function.identity()));

		// Changes, order by the related feature's key
		final var newFeatures = new TreeMap<String, FeaturePlugin>();
		final var updateFeatures = new TreeMap<String, FeaturePlugin>();
		final var removedPlugins = new HashSet<>(plugins.values());

		// Compare with the available plug-in implementing ServicePlugin
		context.getBeansOfType(FeaturePlugin.class).values().forEach(s -> {
			final var plugin = plugins.get(s.getKey());
			if (plugin == null) {
				// New plug-in case
				newFeatures.put(s.getKey(), s);
			} else {
				// Update the artifactId. May have not changed
				plugin.setArtifact(toArtifactId(s));
				if (!plugin.getVersion().equals(getVersion(s))) {
					// The version is different, consider it as an update
					updateFeatures.put(s.getKey(), s);
				}

				// This plug-in has just been handled, so not removed
				removedPlugins.remove(plugin);
			}
		});

		// First install the data of new plug-ins
		updateFeatures.values().forEach(s -> configurePluginUpdate(s, plugins.get(s.getKey())));
		newFeatures.values().forEach(this::configurePluginInstall);

		// Then install/update the plug-in
		update(updateFeatures, plugins);
		installInternal(newFeatures);
		log.info("Plugins are now configured");

		// And remove the old plug-in no more installed
		repository.deleteAll(removedPlugins.stream().map(Persistable::getId).toList());
	}

	/**
	 * Auto install the required plug-ins.
	 *
	 * @param plugins The plug-ins to install.
	 * @return The amount of updated plug-ins.
	 * @throws IOException When plug-ins cannot be updated.
	 */
	public int autoInstall(final Set<String> plugins) throws IOException {
		final var currentPlugins = getPluginClassLoader().getInstalledPlugins();
		final var repositoryName = configuration.get(PLUGIN_REPOSITORY, REPO_CENTRAL);
		var counter = 0;
		for (final var artifact : getLastPluginVersions(repositoryName).values().stream().map(Artifact::getArtifact)
				.filter(plugins::contains).filter(Predicate.not(currentPlugins::containsKey)).toList()) {
			install(artifact, repositoryName);
			counter++;
		}
		return counter;
	}

	/**
	 * Auto update the installed plug-ins.
	 *
	 * @return The amount of updated plug-ins.
	 * @throws IOException When plug-ins cannot be updated.
	 */
	public int autoUpdate() throws IOException {
		final var plugins = getPluginClassLoader().getInstalledPlugins();
		final var repositoryName = configuration.get(PLUGIN_REPOSITORY, REPO_CENTRAL);
		var counter = 0;
		for (final var artifact : getLastPluginVersions(repositoryName).values().stream()
				.filter(a -> plugins.containsKey(a.getArtifact()))
				.filter(a -> PluginsClassLoader.toExtendedVersion(a.getVersion())
						.compareTo(StringUtils.removeStart(plugins.get(a.getArtifact()), a.getArtifact() + "-")) > 0)
				.toList()) {
			install(artifact.getArtifact(), repositoryName);
			counter++;
		}
		return counter;
	}

	/**
	 * Install all ordered plug-ins.
	 */
	private void installInternal(final Map<String, FeaturePlugin> newFeatures) throws Exception {
		for (final var feature : newFeatures.values()) {
			// Do not trigger the installation event when corresponding node is already there
			if (context.getBeansOfType(PluginListener.class).values().stream().allMatch(l -> l.install(feature))) {
				feature.install();
			}
		}
	}

	/**
	 * Update all ordered plug-ins.
	 */
	private void update(final Map<String, FeaturePlugin> updateFeatures, final Map<String, SystemPlugin> plugins)
			throws Exception {
		for (var feature : updateFeatures.entrySet()) {
			feature.getValue().update(plugins.get(feature.getKey()).getVersion());
		}
	}

	/**
	 * Returns a plug-in's last modified time.
	 *
	 * @param plugin The plug-in class. Will be used to find the related container archive or class file.
	 * @return a {@code String} representing the time the file was last modified, or a default time stamp to indicate
	 * the time of last modification is not supported by the file system
	 * @throws URISyntaxException if an I/O error occurs
	 * @throws IOException        if an I/O error occurs
	 */
	protected String getLastModifiedTime(final FeaturePlugin plugin) throws IOException, URISyntaxException {
		return Files
				.getLastModifiedTime(
						Paths.get(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()))
				.toString();
	}

	/**
	 * Configure the updated plug-in in this order :
	 * <ul>
	 * <li>The required entities for the plug-in are persisted. These entities are discovered from
	 * {@link FeaturePlugin#getInstalledEntities()} and related CSV files are load in the data base.</li>
	 * <li>The entity {@link SystemPlugin} is updated to reflect the new version.</li>
	 * </ul>
	 *
	 * @param plugin The newly updated plug-in.
	 * @param entity The current plug-in entity to update.
	 */
	protected void configurePluginUpdate(final FeaturePlugin plugin, final SystemPlugin entity) {
		final var newVersion = getVersion(plugin);
		log.info("Updating the plugin {} v{} -> v{}", plugin.getKey(), entity.getVersion(), newVersion);
		entity.setVersion(newVersion);
	}

	/**
	 * Configure the new plug-in in this order :
	 * <ul>
	 * <li>The required entities for the plug-in are persisted. These entities are discovered from
	 * {@link FeaturePlugin#getInstalledEntities()} and related CSV files are load in the data base.</li>
	 * <li>A new {@link SystemPlugin} is inserted to maintain the validated plug-in and version</li>
	 * </ul>
	 *
	 * @param plugin The newly discovered plug-in.
	 */
	protected void configurePluginInstall(final FeaturePlugin plugin) {
		final var newVersion = getVersion(plugin);
		log.info("Installing the new plugin {} v{}", plugin.getKey(), newVersion);
		try {
			// Build and persist the SystemPlugin entity
			final var entity = new SystemPlugin();
			entity.setArtifact(toArtifactId(plugin));
			entity.setKey(plugin.getKey());
			entity.setVersion(newVersion);
			entity.setType("FEATURE");
			context.getBeansOfType(PluginListener.class).values().forEach(l -> l.configure(plugin, entity));
			repository.saveAndFlush(entity);

			// Configure the plug-in entities
			configurePluginEntities(plugin, plugin.getInstalledEntities());
		} catch (final Exception e) { // NOSONAR - Catch all to notice every time the failure
			// Something happened
			log.error("Installing the new plugin {} v{} failed", plugin.getKey(), newVersion, e);
			throw new TechnicalException(String.format("Configuring the new plugin %s failed", plugin.getKey()), e);
		}
	}

	/**
	 * Guess the Maven artifactId from plug-in artifact name. Use the key and replace the "service" or "feature" part by
	 * "plugin".
	 *
	 * @param plugin The plugin class.
	 * @return The Maven "artifactId" as it should be when naming convention is respected. Required to detect the new
	 * version.
	 */
	public String toArtifactId(final FeaturePlugin plugin) {
		return "plugin-" + Arrays.stream(plugin.getKey().split(":")).skip(1).collect(Collectors.joining("-"));
	}

	/**
	 * Insert the configuration entities of the plug-in. This function can be called multiple times : a check prevent
	 * duplicate entries.
	 *
	 * @param plugin      The related plug-in
	 * @param csvEntities The managed entities where CSV data need to be persisted with this plug-in.
	 * @throws IOException When the CSV management failed.
	 */
	protected void configurePluginEntities(final FeaturePlugin plugin, final List<Class<?>> csvEntities)
			throws IOException {
		//
		final var classLoader = plugin.getClass().getClassLoader();

		// Compute the location of this plug-in, ensuring the
		final var pluginLocation = getPluginLocation(plugin).toString();
		for (final var entityClass : csvEntities) {
			// Build the required CSV file
			final var csv = "csv/"
					+ String.join("-", StringUtils.splitByCharacterTypeCamelCase(entityClass.getSimpleName()))
					.toLowerCase(Locale.ENGLISH)
					+ ".csv";
			configurePluginEntity(Collections.list(classLoader.getResources(csv)).stream(), entityClass,
					pluginLocation);
		}
	}

	/**
	 * Return the file system location corresponding to the given plug-in.
	 *
	 * @param plugin The related plug-in
	 * @return The URL corresponding to the location.
	 */
	protected URL getPluginLocation(final FeaturePlugin plugin) {
		return plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
	}

	/**
	 * Configure a plug-in from a given class and CSV. Its content is converted into target entity's type and inserted
	 * with JPA. The CSV file must be located inside the scope of the target plug-in.
	 *
	 * @param <T>            Target class type.
	 * @param csv            List of URL of CSV. Only the first one matching to plug-in location is read.
	 * @param entityClass    Target class of the CSV.
	 * @param pluginLocation The plug-in owner's location
	 * @throws IOException When the CSV management failed.
	 */
	protected <T> void configurePluginEntity(final Stream<URL> csv, final Class<T> entityClass,
			final String pluginLocation) throws IOException {
		// Accept the CSV file only from the JAR/folder where the plug-in is installed from
		try (var input = new InputStreamReader(
				csv.filter(u -> u.getPath().startsWith(pluginLocation) || u.toString().startsWith(pluginLocation))
						.findFirst()
						.orElseThrow(() -> new TechnicalException(
								String.format("Unable to find CSV file for entity %s", entityClass.getSimpleName())))
						.openStream(),
				StandardCharsets.UTF_8)) {

			// Build and save the entities managed by this plug-in
			csvForJpa.toJpa(entityClass, input, true, false, e -> {
				// Need brace because of ambiguous signature consumer/Function
				persistAsNeeded(entityClass, e);
			});
			em.flush();
			em.clear();
		}

	}

	/**
	 * Persist the given entity only if it is not yet persisted. This is not an update mode.
	 *
	 * @param entityClass The entity class to persist.
	 * @param entity      The entity read from the CSV, and to persist.
	 * @param <T>         The entity type.
	 */
	protected <T> void persistAsNeeded(final Class<T> entityClass, T entity) {
		if (entity instanceof AbstractBusinessEntity<?> be) {
			persistAsNeeded(entityClass, be);
		} else if (entity instanceof INamableBean<?> nb) {
			persistAsNeeded(entityClass, entity, "name", nb.getName());
		} else if (entity instanceof SystemUser su) {
			persistAsNeeded(entityClass, entity, "login", su.getLogin());
		} else {
			em.persist(entity);
		}
	}

	private <T> void persistAsNeeded(final Class<T> entityClass, AbstractBusinessEntity<?> entity) {
		// Check for duplicate before the insert
		if (em.find(entityClass, entity.getId()) == null) {
			em.persist(entity);
		}
	}

	private <T> void persistAsNeeded(final Class<T> entityClass, Object entity, final String property,
			final String value) {
		if (em.createQuery("SELECT 1 FROM " + entityClass.getName() + " WHERE " + property + " = :value")
				.setParameter("value", value).getResultList().isEmpty()) {
			em.persist(entity);
		}
	}

	/**
	 * Return a fail-safe computed version of the given {@link FeaturePlugin}
	 *
	 * @param plugin The plug-in instance
	 * @return The version from the MANIFEST or the timestamp. <code>?</code> when an error occurs.
	 */
	protected String getVersion(final FeaturePlugin plugin) {
		return Optional.ofNullable(plugin.getVersion()).orElseGet(() -> {
			// Not explicit version
			try {
				return getLastModifiedTime(plugin);
			} catch (final IOException | URISyntaxException e) {
				log.warn("Unable to determine the version of plug-in {}", plugin.getClass(), e);

				// Fail-safe version
				return "?";
			}
		});
	}

	@Override
	public void decorate(final SessionSettings settings) {
		// Add the enabled plug-ins
		if (settings.getApplicationSettings().getPlugins() == null) {
			settings.getApplicationSettings().setPlugins(context.getBeansOfType(FeaturePlugin.class).values().stream()
					.map(FeaturePlugin::getKey).toList());
		}

	}

}
