package org.ligoj.bootstrap.core.dao;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.boot.archive.scan.internal.StandardScanner;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.scan.spi.ScanParameters;
import org.hibernate.boot.archive.scan.spi.ScanResult;

/**
 * Special scanner handling the VFS protocol.
 * 
 * @author Fabrice Daugan
 */
public class ResourceScanner extends StandardScanner {

	/**
	 * ORM file path.
	 */
	public static final String META_INF_ORM_XML = "META-INF/orm.xml";

	/**
	 * Perform the scanning against the described persistence unit using the defined options, and return the scan
	 * results.
	 * 
	 * @param scanOptions
	 *            The scan options
	 * @return The scan results.
	 */
	@Override
	public ScanResult scan(final ScanEnvironment environment, final ScanOptions scanOptions, final ScanParameters parameters) {

		try {
			final Set<URL> urls = new LinkedHashSet<>();
			urls.addAll(environment.getNonRootUrls());
			urls.addAll(Collections.list(getOrmUrls()).stream().map(ormUrl-> {
				try {
					return getJarUrl(ormUrl);
				} catch (final MalformedURLException e) {
					throw new IllegalStateException("Unable to read ORM file from jar", e);
				}
			}).collect(Collectors.toList()));

			// Remove the root URL from the non root.
			urls.remove(environment.getRootUrl());

			// Replace the URL with the new set
			environment.getNonRootUrls().clear();
			environment.getNonRootUrls().addAll(urls);
			return super.scan(environment, scanOptions, parameters);
		} catch (final IOException e) {
			throw new IllegalStateException("Unable to read ORM Jars", e);
		}
	}

	/**
	 * Return JAR URL from ORM URL.
	 * 
	 * @param ormUrl
	 *            ORM URL.
	 * @return the URL of JAR containing the given ORM file.
	 * @throws MalformedURLException
	 *             if JAR URL cannot be built from the ORM.
	 */
	protected URL getJarUrl(final URL ormUrl) throws MalformedURLException {
		final URL ormJarUrl;
		if (ormUrl.getProtocol().equals("jar")) {
			// Extract the jar containing this file
			ormJarUrl = new URL("file", ormUrl.getHost(), ormUrl.getPath().substring("file:".length(), ormUrl.getPath().indexOf('!')));
		} else {
			// Remove the trailing path
			ormJarUrl = new URL(ormUrl.getProtocol(), ormUrl.getHost(),
					ormUrl.getPath().substring(0, ormUrl.getPath().length() - META_INF_ORM_XML.length() - 1));
		}
		return ormJarUrl;
	}

	/**
	 * Return existing ORM resources.
	 * 
	 * @return existing ORM resources found in classpath.
	 * @param IOException
	 *            from {@link ClassLoader#getResources(String)}
	 */
	protected Enumeration<URL> getOrmUrls() throws IOException {
		return Thread.currentThread().getContextClassLoader().getResources(META_INF_ORM_XML);
	}

}
