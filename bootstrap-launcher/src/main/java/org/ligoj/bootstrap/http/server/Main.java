/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Jetty Http server launcher.
 */
@Slf4j
@Getter
public final class Main {

	private static final String SETTINGS = "META-INF/jetty/jetty-dev.properties";

	/**
	 * Attached server instance.
	 */
	private final Server server;

	/**
	 * Attached the last started server instance. Take care of thread safe issues. This enables server shutdown.
	 */
	@Getter
	private static Server lastStartedServer;

	/**
	 * Constructor : load property and XmlConfiguration
	 *
	 * @throws Exception server start error.
	 */
	public Main() throws Exception {
		server = new Server();
		final var jettyPropertiesFile = System.getProperty("jetty.properties", SETTINGS);
		try (var ignored = configure(jettyPropertiesFile)) {
			// Load the properties file
			log.debug("Loading Jetty Settings from {}", SETTINGS);
		}
	}

	/**
	 * Configure the server from properties and XML.
	 */
	private InputStream configure(final String jettyPropertiesFile) throws Exception {
		final var propertiesInput = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(jettyPropertiesFile);
		if (propertiesInput == null) {
			log.error("Unable to find jetty properties file : " + jettyPropertiesFile);
		} else {

			// Copy the properties
			copyProperties(propertiesInput);

			// Configure the server
			new XmlConfiguration(Resource.newResource(Thread.currentThread().getContextClassLoader()
					.getResource(System.getProperty("jetty.xml", "META-INF/jetty/jetty.xml")))).configure(server);
		}
		return propertiesInput;
	}

	/**
	 * Copy properties from the given input.
	 */
	private void copyProperties(final InputStream propertiesInput) throws IOException {
		final var properties = new Properties();
		properties.load(propertiesInput);
		properties.putAll(System.getProperties());
		System.setProperties(properties);
	}

	// CHECKSTYLE:OFF This is a real application entry

	/**
	 * Main launcher. Equals to <code>mvn jetty:start</code> command.
	 *
	 * @param args by design arguments, but not used.
	 * @throws Exception server start error.
	 */
	public static void main(final String... args) throws Exception {
		// CHECKSTYLE:ON
		final var main = new Main();
		main.server.start();

		// Update the last started server instance.
		lastStartedServer = main.server;
		main.server.join();
	}
}