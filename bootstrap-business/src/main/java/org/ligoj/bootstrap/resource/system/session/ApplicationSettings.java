/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * Application settings.
 */
@Component
@Getter
public class ApplicationSettings {

	/**
	 * Build number associated to SCM UID revision. #
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

}
