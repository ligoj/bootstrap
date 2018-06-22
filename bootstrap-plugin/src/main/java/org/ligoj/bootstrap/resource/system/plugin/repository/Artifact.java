/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.repository;

import java.io.Serializable;

/**
 * A Maven artifact.
 */
public interface Artifact extends Serializable {

	/**
	 * Artifact digit version. Not "LATEST",...
	 * 
	 * @return Artifact digit version. Not "LATEST",...
	 */
	String getVersion();

	/**
	 * Artifact identifier.
	 * 
	 * @return Artifact identifier.
	 */
	String getArtifact();
}
