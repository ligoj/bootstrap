/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.session;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Application settings.
 */
@Component
@Getter
public class ApplicationSettings {

	/**
	 * Build number associated to SCM UID revision.
	 */
	@Value("${project.buildNumber}")
	private String buildNumber;

	/**
	 * Build time stamp defined in milliseconds
	 */
	@Value("${project.timestamp}")
	private String buildTimestamp;

	/**
	 * Build version of the application as defined in the build configuration (Maven).
	 */
	@Value("${project.version}")
	private String buildVersion;

	/**
	 * Application version digest used as hash. Represent a version identifier considering the application version and
	 * the enabled plug-ins. Used for cached resources.
	 */
	@Value("${project.version.digest}")
	private String digestVersion;

	/**
	 * Code the plug-ins need to contribute at session successful time. Used generally to contribute to the top level
	 * menu.
	 */
	@Value("${project.bootstrap.private}")
	private String bootstrapPrivateCode;

	/**
	 * Enabled plug-in simple name. Not keys.
	 */
	@Setter
	private List<String> plugins;
}
