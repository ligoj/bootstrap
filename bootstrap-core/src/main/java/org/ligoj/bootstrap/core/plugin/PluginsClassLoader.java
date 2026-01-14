/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class Loader which load jars in {@value #PLUGINS_DIR} directory inside the home directory.
 */
@Slf4j
public class PluginsClassLoader extends URLClassLoader {

	/**
	 * Safe mode property flag.
	 */
	public static final String ENABLED_PROPERTY = "ligoj.plugin.enabled";

	/**
	 * System property name pointing to the home directory. When undefined, system user home directory will be used
	 */
	public static final String HOME_DIR_PROPERTY = "ligoj.home";

	/**
	 * Default home directory part used in addition of system user home directory : "/home/my-user" for sample.
	 */
	public static final String HOME_DIR_FOLDER = ".ligoj";
	/**
	 * Plug-ins directory inside the home property
	 */
	public static final String PLUGINS_DIR = "plugins";
	/**
	 * Plug-ins export directory inside the home property
	 */
	public static final String EXPORT_DIR = "export";

	/**
	 * Optional file containing the code to execute when the private zone is being loaded.
	 */
	public static final String BOOTSTRAP_PRIVATE_FILE = "META-INF/resources/webjars/bootstrap.private.js";

	/**
	 * Pattern used to extract the version from a JAR plug-in file name.
	 */
	public static final Pattern VERSION_PATTERN = Pattern
			.compile("(-(\\d[\\da-zA-Z]*(\\.[\\da-zA-Z]+){1,3}(-SNAPSHOT)?))\\.jar$");

	/**
	 * The application home directory.
	 */
	@Getter
	private final Path homeDirectory;

	/**
	 * The plug-in directory, inside the home directory.
	 */
	@Getter
	private final Path pluginDirectory;

	@Getter
	private final String digestVersion;

	/**
	 * Read only plug-in safe mode. When <code>false</code>, external plug-ins are not participating in the classpath.
	 */
	@Getter
	protected final boolean enabled;

	private final ClassLoader parent;

	/**
	 * Initialize the plug-in {@link URLClassLoader} and the related directories.
	 *
	 * @throws IOException              exception when reading plug-ins directory
	 * @throws NoSuchAlgorithmException MD5 digest is unavailable for version ciphering.
	 */
	public PluginsClassLoader() throws IOException, NoSuchAlgorithmException {
		super(new URL[0], Thread.currentThread().getContextClassLoader());
		this.parent = Thread.currentThread().getContextClassLoader();
		this.enabled = Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY, "true"));
		this.homeDirectory = computeHome();
		this.pluginDirectory = this.homeDirectory.resolve(PLUGINS_DIR);

		// Create the plug-in directory as needed
		log.info("Initialize the plug-ins from directory from {}", homeDirectory);
		Files.createDirectories(this.pluginDirectory);

		// Add the home itself in the class-path
		addURL(this.homeDirectory.toUri().toURL());

		if (!isEnabled()) {
			// Ignore this refresh keep original class-path
			log.info("SAFE MODE - Plugins classloader is disabled");
			this.digestVersion = Base64.getEncoder().encodeToString(String.valueOf(Math.random()).getBytes());
			return;
		}

		this.digestVersion = completeClasspath();
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		Objects.requireNonNull(name);
		@SuppressWarnings("unchecked") final var tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
		tmp[0] = findResources(name);
		tmp[1] = parent.getResources(name);
		return new ConcatEnumeration<>(tmp);
	}

	@Override
	public URL getResource(String name) {
		Objects.requireNonNull(name);
		var url = findResource(name);
		if (url == null) {
			url = parent.getResource(name);
		}
		return url;
	}

	/**
	 * Virtualize a list of {@link Enumeration} into a single one.
	 */
	static final class ConcatEnumeration<E> implements Enumeration<E> {
		private final Enumeration<E>[] enums;
		private int index = 0;

		public ConcatEnumeration(Enumeration<E>[] enums) {
			this.enums = enums;
		}

		@Override
		public boolean hasMoreElements() {
			while (index < enums.length) {
				if (enums[index].hasMoreElements()) {
					return true;
				}

				// Next chunk
				index++;
			}

			// End of chunks
			return false;
		}

		@Override
		public E nextElement() {
			if (hasMoreElements()) {
				return enums[index].nextElement();
			}
			throw new NoSuchElementException();
		}
	}

	/**
	 * Complete the class-path with plug-ins jars
	 *
	 * @return The version digest.
	 * @throws NoSuchAlgorithmException MD5 digest is unavailable for version ciphering.
	 */
	private String completeClasspath() throws IOException, NoSuchAlgorithmException {
		// Mapping from "version file" to Path
		// Key : The filename without extension and with extended comparable version
		// Value : The resolved Path
		final var versionFileToPath = new HashMap<String, Path>();

		// Ordered last version (to be enabled) plug-ins.
		final var enabledPlugins = getInstalledPlugins(versionFileToPath, false);
		final var buffer = new StringBuilder();

		// Add the filtered plug-in files to the class-path and deploy them
		for (final var plugin : enabledPlugins.entrySet()) {
			final var pluginPath = versionFileToPath.get(plugin.getValue());
			final var uri = pluginPath.toUri();
			log.debug("Add plugin {}", uri);
			copyExportedResources(plugin.getKey(), pluginPath);
			addURL(uri.toURL());
			buffer.append(plugin.getValue());
		}

		// Build a digestion version
		final var mDigest = MessageDigest.getInstance("MD5");
		mDigest.update(buffer.toString().getBytes());
		final var digest = Base64.getEncoder().encodeToString(mDigest.digest());
		System.setProperty("project.version.digest", digest);

		// Expose the bootstrap code compiled from all plug-ins
		final var boots = new StringBuilder();
		for (final var bootUrl : Collections.list(getResources(BOOTSTRAP_PRIVATE_FILE))) {
			boots.append(getBootstrapCode(bootUrl)).append('\n');
		}
		System.setProperty("project.bootstrap.private", boots.toString());

		log.info("Plugins ClassLoader has added {} plug-ins and ignored {} old plug-ins, digest {}",
				enabledPlugins.size(), versionFileToPath.size() - enabledPlugins.size(), digest);
		return digest;
	}

	/**
	 * Return the mapping of the elected last plug-in name to the corresponding version file.
	 *
	 * @param versionFileToPath The mapping filled by this method. Key : The filename without extension and with
	 *                          extended comparable version. Value : The resolved Path.
	 * @param javadocFiler      When true, only Javadoc jar are analyzed, otherwise they are excluded.
	 * @return The mapping of the elected last plug-in name to the corresponding version file. Key: the plug-in
	 * artifactId resolved from the filename. Value: the plug-in artifactId with its extended comparable
	 * version. The return keys are alphabetically ordered with natural dependency respect.
	 * @throws IOException When file list failed.
	 */
	public Map<String, String> getInstalledPlugins(final Map<String, Path> versionFileToPath, final boolean javadocFiler) throws IOException {
		final var versionFiles = new TreeMap<String, String>();
		try (var list = Files.list(this.pluginDirectory)) {
			list.filter(p -> javadocFiler ? p.toString().endsWith("-javadoc.jar") : (p.toString().endsWith(".jar") && !p.toString().endsWith("-javadoc.jar")))
					.forEach(path -> addVersionFile(versionFileToPath, versionFiles, path));
		}
		final var enabledPlugins = new TreeMap<String, String>(Comparator.reverseOrder());

		// Remove old plug-in from the list
		versionFiles.keySet().stream().sorted(Comparator.reverseOrder())
				.forEach(p -> enabledPlugins.putIfAbsent(versionFiles.get(p), p));
		return enabledPlugins;
	}

	/**
	 * Return the mapping of the installed plug-ins. Only the last version is returned.
	 *
	 * @return The mapping of the elected last plug-in name to the corresponding version file. Key: the plug-in
	 * artifactId resolved from the filename. Value: the plug-in artifactId with its extended comparable
	 * version.
	 * @throws IOException When file list failed.
	 */
	public Map<String, String> getInstalledPlugins() throws IOException {
		return getInstalledPlugins(new HashMap<>(), false);
	}

	/**
	 * Return the plug-in class loader from the current class loader.
	 *
	 * @return the closest {@link PluginsClassLoader} instance from the current thread's {@link ClassLoader}. May be
	 * <code>null</code>.
	 */
	public static PluginsClassLoader getInstance() {
		return getInstance(Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Return the plug-in class loader from the given class loader's hierarchy.
	 *
	 * @param cl The {@link ClassLoader} to inspect.
	 * @return the closest {@link PluginsClassLoader} instance from the current thread's {@link ClassLoader}. May be
	 * <code>null</code>.
	 */
	public static PluginsClassLoader getInstance(final ClassLoader cl) {
		if (cl == null) {
			// A separate class loader ?
			log.info("PluginsClassLoader requested but not found in the current classloader hierarchy {}",
					Thread.currentThread().getContextClassLoader().toString());
			return null;
		}
		if (cl instanceof PluginsClassLoader pcl) {
			// Class loader has been found
			return pcl;
		}

		// Try the parent
		return getInstance(cl.getParent());
	}

	/**
	 * The content of the bootstrap file.
	 *
	 * @param file The bootstrap code URL of a plug-in.
	 * @return The content of the file.
	 * @throws IOException When plug-in file cannot be read.
	 * @see #BOOTSTRAP_PRIVATE_FILE
	 */
	protected String getBootstrapCode(final URL file) throws IOException {
		try (var in = file.openStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Copy resources needed to be exported from the JAR plug-in to the home.
	 *
	 * @param plugin     The plug-in identifier.
	 * @param pluginFile The target plug-in file.
	 * @throws IOException When plug-in file cannot be read.
	 */
	protected void copyExportedResources(final String plugin, final Path pluginFile) throws IOException {
		try (var fileSystem = FileSystems.newFileSystem(pluginFile, this)) {
			final var export = fileSystem.getPath("/" + EXPORT_DIR);
			if (Files.exists(export)) {
				final var targetExport = getHomeDirectory().resolve(EXPORT_DIR);
				try (var walk = Files.walk(export)) {
					walk.forEach(from -> copyExportedResource(plugin, targetExport, export, from));
				}
			}
		}
	}

	/**
	 * Copy a resource as needed to be exported from the JAR plug-in to the home.
	 */
	private void copyExportedResource(final String plugin, final Path targetExport, final Path root, final Path from) {
		final var to = targetExport.resolve(root.relativize(from).toString());
		// Copy without overwrite
		if (!to.toFile().exists()) {
			try {
				copy(from, to);
			} catch (final IOException e) {
				throw new PluginException(plugin, String.format("Unable to copy exported resource %s to %s", from, to), e);
			}
		}
	}

	/**
	 * Copy a resource needed to be exported from the JAR plug-in to the home.
	 *
	 * @param from The source file to the destination file. Directories are not supported.
	 * @param to   The destination file.
	 * @throws IOException When plug-in file cannot be copied.
	 */
	protected void copy(final Path from, final Path to) throws IOException {
		if (Files.isDirectory(from)) {
			Files.createDirectories(to);
		} else {
			Files.copy(from, to);
		}
	}

	private void addVersionFile(final Map<String, Path> versionFileToPath, final Map<String, String> versionFiles,
			final Path path) {
		final var file = path.getFileName().toString();
		final var matcher = VERSION_PATTERN.matcher(file);
		final String noVersionFile;
		final String fileWithExtVersion;
		if (matcher.find()) {
			// This plug-in has a version, extend the version for the next natural string ordering
			noVersionFile = file.substring(0, matcher.start());
			fileWithExtVersion = noVersionFile + "-" + toExtendedVersion(matcher.group(1));
		} else {
			// No version, the file will be kept with the lowest level version number
			noVersionFile = FilenameUtils.removeExtension(file);
			fileWithExtVersion = noVersionFile + "-0";
		}

		// Store the version files to keep later only the most recent one
		versionFileToPath.put(fileWithExtVersion, path);
		versionFiles.put(fileWithExtVersion, noVersionFile);
	}

	/**
	 * Convert a version to a comparable string and following the 'server' specification. Maximum 4 version ranges are
	 * accepted.
	 *
	 * @param version The version string to convert. May be <code>null</code>
	 * @return The given version to be comparable with another version. Handle the 'SNAPSHOT' case considered as older
	 * than the one without this suffix.
	 * @see PluginsClassLoader#toExtendedVersion(String)
	 */
	public static String toExtendedVersion(final String version) {
		final var fileWithVersionExp = new StringBuilder();
		final var allFragments = new String[]{"0", "0", "0", "0"};
		final var versionFragments = ObjectUtils.getIfNull(StringUtils.split(version, "-."), allFragments);
		System.arraycopy(versionFragments, 0, allFragments, 0, versionFragments.length);
		Arrays.stream(allFragments).map(s -> StringUtils.leftPad(StringUtils.leftPad(s, 7, '0'), 8, 'Z'))
				.forEach(fileWithVersionExp::append);
		return fileWithVersionExp.toString();
	}

	/**
	 * Compute the right home directory for the application from the system properties.
	 *
	 * @return The computed home directory.
	 */
	protected Path computeHome() {
		final Path homeDir;
		if (System.getProperty(HOME_DIR_PROPERTY) == null) {
			// Non-standard home directory
			homeDir = Paths.get(System.getProperty("user.home"), HOME_DIR_FOLDER);
			log.info(
					"Home directory is '{}', resolved from current home user location. Use '{}' system property to override this path",
					homeDir, HOME_DIR_PROPERTY);
		} else {
			// Home directory inside the system user's home directory
			homeDir = Paths.get(System.getProperty(HOME_DIR_PROPERTY));
			log.info("Home directory is '{}', resolved from the system property '{}'", homeDir, HOME_DIR_PROPERTY);
		}
		return homeDir;
	}

	/**
	 * Convert a fragment list to a {@link Path} inside the home directory. The intermediate directories are also
	 * created.
	 *
	 * @param fragments The file fragments within the home directory.
	 * @return The {@link Path} reference.
	 * @throws IOException When the parent directories creation failed.
	 * @since 2.2.4
	 */
	public Path toPath(final String... fragments) throws IOException {
		return toPath(getHomeDirectory(), fragments);
	}

	/**
	 * Get a file reference inside the given parent path. The parent directories are created as needed.
	 *
	 * @param parent    The parent path.
	 * @param fragments The file fragments within the given parent.
	 * @return The {@link Path} reference.
	 * @throws IOException When the parent directories creation failed.
	 */
	protected Path toPath(final Path parent, final String... fragments) throws IOException {
		var parentR = parent;
		for (var fragment : fragments) {
			parentR = parentR.resolve(fragment);
		}
		// Ensure the parent path is created
		FileUtils.forceMkdir(parentR.getParent().toFile());
		return parentR;
	}
}
