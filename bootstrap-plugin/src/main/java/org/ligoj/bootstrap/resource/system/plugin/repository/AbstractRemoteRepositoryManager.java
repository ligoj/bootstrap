/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Objects;

/**
 * Base class for remote repository manager.
 */
@Slf4j
public abstract class AbstractRemoteRepositoryManager implements RepositoryManager {

	/**
	 * Injected configuration resource shared with child classes.
	 */
	@Autowired
	protected ConfigurationResource configuration;

	/**
	 * Return the plug-ins search URL.
	 *
	 * @param defaultUrl The default URL.
	 * @return The plug-ins search URL.
	 */
	protected String getSearchUrl(final String defaultUrl) {
		return getConfiguration("search.url", defaultUrl);
	}

	/**
	 * Return the proxy host used for search.
	 *
	 * @return The proxy host used for search.
	 */
	protected String getSearchProxyHost() {
		return getConfiguration("search.proxy.host", null);
	}

	/**
	 * Return the proxy host used for search.
	 *
	 * @return The proxy port used for search.
	 */
	protected int getSearchProxyPort() {
		return Integer.parseInt(StringUtils.defaultIfBlank(Objects.toString(getConfiguration("search.proxy.port", "8080")), "8080"));
	}

	/**
	 * Return the plug-ins filtered group-id to query the repository manager.
	 *
	 * @param defaultGroupIp The "groupId".
	 * @return The "groupId" filter.
	 */
	protected String getGroupId(final String defaultGroupIp) {
		return getConfiguration("groupId", defaultGroupIp);
	}

	/**
	 * Return the plug-ins download base URL.
	 *
	 * @param defaultUrl The default URL.
	 * @return The plug-ins download URL.
	 */
	protected String getArtifactBaseUrl(final String defaultUrl) {
		return getConfiguration("artifact.url", defaultUrl);
	}

	/**
	 * Return the proxy host used for download.
	 *
	 * @return The proxy host used for download.
	 */
	protected String getArtifactProxyHost() {
		return getConfiguration("artifact.proxy.host", null);
	}

	/**
	 * Return the proxy host used for download.
	 *
	 * @return The proxy port used for download.
	 */
	protected int getArtifactProxyPort() {
		return Integer.parseInt(StringUtils.defaultIfBlank(Objects.toString(getConfiguration("artifact.proxy.port", "8080")), "8080"));
	}

	/**
	 * Get the repository configuration or the default value if not configured.
	 *
	 * @param suffix       The configuration key name suffix.
	 * @param defaultValue The default configuration value.
	 * @return The configuration value. Default is the given "defaultValue" parameter.
	 */
	private String getConfiguration(final String suffix, final String defaultValue) {
		return StringUtils.defaultIfBlank(configuration.get("plugins.repository-manager." + getId() + "." + suffix, defaultValue), defaultValue);
	}

	/**
	 * Return the plug-ins download URL.
	 *
	 * @param artifact   The Maven artifact identifier and also corresponding to the plug-in simple name.
	 * @param version    The version to install.
	 * @param defaultUrl The default artifact base URL.
	 * @return The plug-ins download URL. Ends with "/".
	 */
	protected String getArtifactUrl(String artifact, String version, final String defaultUrl) {
		return StringUtils.appendIfMissing(getArtifactBaseUrl(defaultUrl), "/") + artifact + "/" + version + "/"
				+ artifact + "-" + version + ".jar";
	}

	/**
	 * Return the input stream from the remote URL.
	 *
	 * @param artifact   The Maven artifact identifier and also corresponding to the plug-in simple name.
	 * @param version    The version to install.
	 * @param defaultUrl The default artifact base URL.
	 * @return The opened {@link InputStream} of the artifact to download.
	 * @throws IOException When download failed.
	 * @see #getArtifactBaseUrl(String)
	 */
	public InputStream getArtifactInputStream(String artifact, String version, final String defaultUrl)
			throws IOException {
		final var url = getArtifactUrl(artifact, version, defaultUrl);
		log.info("Resolved remote URL is {}", url);
		final var urlObj = URI.create(url).toURL();
		final var proxyHost = getArtifactProxyHost();
		final Proxy proxy;
		if (proxyHost == null) {
			proxy = Proxy.NO_PROXY;
		} else {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, getArtifactProxyPort()));
		}
		return urlObj.openConnection(proxy).getInputStream();
	}

}
