/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Result from maven search
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusSearchResult implements Artifact {
	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("artifactId")
	private String artifact;

	@JsonProperty("latestRelease")
	private String version;
}