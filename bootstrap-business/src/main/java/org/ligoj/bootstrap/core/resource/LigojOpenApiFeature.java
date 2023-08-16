/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;

import org.apache.cxf.annotations.Provider;
import org.apache.cxf.jaxrs.swagger.ui.SwaggerUiConfig;
import org.springframework.beans.factory.annotation.Value;

/**
 * OpenAPI feature customized for application.
 */
@Provider(value = Provider.Type.Feature, scope = Provider.Scope.Server)
public class LigojOpenApiFeature extends org.apache.cxf.jaxrs.openapi.OpenApiFeature {

	/**
	 * Default constructor with version.
	 *
	 * @param version The current application version.
	 */
	public LigojOpenApiFeature(@Value("${project.version}") final String version) {
		setLicense("MIT");
		setLicenseUrl("https://github.com/ligoj/ligoj/blob/master/LICENSE");
		setTitle("Ligoj API application");
		setContactName("The Ligoj team");
		setContactUrl("https://github.com/ligoj");
		setDescription("REST API services of application. Includes the core services and the features of actually loaded plugins");
		setVersion(version);
		setSwaggerUiConfig(new SwaggerUiConfig().url("openapi.json").queryConfigEnabled(false));
	}
}
