/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Result of a master secret rotation.
 */
@Getter
@Setter
public class RotationVo {

	/**
	 * Amount of values encrypted again with the new master secret.
	 */
	private int count;

	/**
	 * The new master secret, only when it could not be saved in its source: neither an external secret manager,
	 * nor a writable file. It has to be provided to the next start of the application, and is not available
	 * anywhere else.
	 */
	private String secret;

	/**
	 * Names of the properties of the properties files having an <code>ENC(...)</code> value. These values are not
	 * updated by the rotation: they have to be encrypted again and replaced before the next start of the application.
	 */
	private List<String> properties;
}
