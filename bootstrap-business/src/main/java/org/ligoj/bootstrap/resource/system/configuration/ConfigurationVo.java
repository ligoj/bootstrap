/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import org.ligoj.bootstrap.model.system.SystemConfiguration;

import lombok.Getter;
import lombok.Setter;

/**
 * "Configuration" value with source information.
 */
@Getter
@Setter
public class ConfigurationVo extends SystemConfiguration {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * When <code>true</code> is backed by an entity.
	 */
	private boolean persisted;

	/**
	 * When <code>true</code> the value is secured and need to be read on-demand.
	 */
	private boolean secured;

	/**
	 * When <code>true</code> the value is defined in system and persisted and different from the database value.
	 */
	private boolean overridden;

	/**
	 * The property source.
	 */
	private String source;

}
