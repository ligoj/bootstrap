/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.PathResourceFactory;
import org.eclipse.jetty.util.resource.VisibleCombinedResource;

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
		configure(jettyPropertiesFile);
		// Load the properties file
		log.debug("Loading Jetty Settings from {}", SETTINGS);
	}

	/**
	 * Configure the server from properties and XML.
	 */
	private void configure(final String jettyPropertiesFile) throws Exception {
		try (final var propertiesInput = Thread.currentThread().getContextClassLoader().getResourceAsStream(jettyPropertiesFile)) {
			if (propertiesInput == null) {
				log.error("Unable to find jetty properties file : " + jettyPropertiesFile);
			} else {
				// Copy the properties
				copyProperties(propertiesInput);

				if (System.getProperty("jetty.port") != null) {
					// Configure the server
					final var httpConfig = new org.eclipse.jetty.server.HttpConfiguration();
					httpConfig.setSecureScheme("https");
					httpConfig.setSecurePort(8443);
					httpConfig.setOutputBufferSize(32768);
					httpConfig.setResponseHeaderSize(8192);
					httpConfig.setRequestHeaderSize(8192);

					final var factory = new org.eclipse.jetty.server.HttpConnectionFactory(httpConfig);
					final var connector = new org.eclipse.jetty.server.ServerConnector(server, factory);
					connector.setHost(System.getProperty("jetty.host", "localhost"));
					connector.setPort(Integer.parseInt(System.getProperty("jetty.port", "8080"),10));
					connector.setIdleTimeout(30000);
					server.setConnectors(new Connector[]{connector});
				}

				// Declare server handler collection
				if (System.getProperty("jetty.webapp") != null) {
					final var contexts = new ContextHandlerCollection();
					server.setHandler(contexts);

					final var webContext = new org.eclipse.jetty.ee10.webapp.WebAppContext();
					webContext.setContextPath(System.getProperty("jetty.contextPath", "/bootstrap-business"));
					webContext.setDescriptor(System.getProperty("jetty.descriptor"));

					final var pathFactory = new PathResourceFactory();
					final var resourceCollection = VisibleCombinedResource.combine(
							pathFactory.newResource(System.getProperty("jetty.webapp", ".")),
							pathFactory.newResource(System.getProperty("jetty.target", "./WEB-INF/classes"))
					);
					webContext.setBaseResource(resourceCollection);
					webContext.setResourceAlias("/WEB-INF/classes/", "/classes/");
					webContext.setExtraClasspath("./target/classes/");
					contexts.addHandler(webContext);
				}

				server.setAttribute("stopAtShutdown", true);

			}
		}
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