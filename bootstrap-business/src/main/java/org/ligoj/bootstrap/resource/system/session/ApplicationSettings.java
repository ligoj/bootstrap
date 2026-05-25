/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.session;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application settings.
 */
@Component
@Getter
public class ApplicationSettings {

	/**
	 * Display name of the application, surfaced in the SPA's sidebar brand,
	 * the About view title, and the licence dialog. Resolved from the
	 * {@code ligoj.name} property at startup and defaults to {@code "Ligoj"}
	 * when the host hasn't configured a rebrand. Static (read at boot from
	 * the Spring environment) — switching brands requires a restart, which
	 * is consistent with the other build-time settings on this bean.
	 */
	@Value("${ligoj.name:Ligoj}")
	private String name;

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

	/**
	 * Extra-data available for all sessions.
	 */
	private final Map<String, String> data = new ConcurrentHashMap<>();

}
