/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;

import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.jaxrs.model.doc.JavaDocProvider;
import org.apache.cxf.jaxrs.openapi.OpenApiCustomizer;
import org.apache.cxf.jaxrs.swagger.ui.SwaggerUiConfig;
import org.ligoj.bootstrap.core.plugin.PluginsClassLoader;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI feature customized for application.
 */
@Provider(value = Provider.Type.Feature, scope = Provider.Scope.Server)
@Slf4j
public class LigojOpenApiFeature extends org.apache.cxf.jaxrs.openapi.OpenApiFeature {
	/**
	 * Return the current plug-in class loader.
	 *
	 * @return The current plug-in class loader.
	 */
	protected PluginsClassLoader getPluginClassLoader() {
		return PluginsClassLoader.getInstance();
	}

	/**
	 * Default constructor with version.
	 *
	 * @param version The current application version.
	 */
	@SuppressWarnings({"this-escape"})
	public LigojOpenApiFeature(@Value("${project.version}") final String version) throws IOException {
		setLicense("MIT");
		setLicenseUrl("https://github.com/ligoj/ligoj/blob/master/LICENSE");
		setTitle("Ligoj API application");
		setContactName("The Ligoj team");
		setContactUrl("https://github.com/ligoj");
		setDescription("REST API services of application. Includes the core services and the features of actually loaded plugins");
		setVersion(version);
		setSwaggerUiConfig(new SwaggerUiConfig().url("openapi.json").queryConfigEnabled(false));
		setSecurityDefinitions(Map.of(
				"api_key", new SecurityScheme().description("API Key").in(SecurityScheme.In.HEADER).type(SecurityScheme.Type.APIKEY).name("api-key"),
				"api_user", new SecurityScheme().description("API User").in(SecurityScheme.In.HEADER).type(SecurityScheme.Type.APIKEY).name("api-user")));
		addJavadoc();
	}

	void addJavadoc() throws IOException {

		// Check the available class loader for javadoc contribution
		final var classloader = getPluginClassLoader();
		if (classloader == null) {
			log.info("Plugin classLoader is not available, no javadoc providers discovery");
			return;
		}

		final var versionFileToPath = new HashMap<String, Path>();
		final var pluginFiles = classloader.getInstalledPlugins(versionFileToPath, true);
		final var javadocUrls = new ArrayList<URL>();
		for (final var path : pluginFiles.values()) {
			final var javadocPath = versionFileToPath.get(path);
			javadocUrls.add(javadocPath.toUri().toURL());
		}

		// Add resolved Javadoc URLs
		log.info("Adding javadoc providers of {} locations", javadocUrls.size());
		final var customizer = new OpenApiCustomizer() {
			@Override
			public OpenAPIConfiguration customize(final OpenAPIConfiguration configuration) {
				super.customize(configuration);
				configuration.getOpenAPI().setServers(List.of(new Server().url("./").description("REST API Server")));
				return configuration;
			}
		};
		customizer.setDynamicBasePath(false);
		customizer.setJavadocProvider(new JavaDocProvider(javadocUrls.toArray(new URL[0])));
		setCustomizer(customizer);
	}
}
