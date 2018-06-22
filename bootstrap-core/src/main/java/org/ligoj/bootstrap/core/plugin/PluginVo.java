/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import org.ligoj.bootstrap.core.NamedBean;
import org.ligoj.bootstrap.model.system.SystemPlugin;

import lombok.Getter;
import lombok.Setter;

/**
 * Plug-in information. The "id" property correspond to the related plug-in's key.
 */
@Getter
@Setter
public class PluginVo extends NamedBean<String> {
	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Related plug-in entity.
	 */
	private SystemPlugin plugin;

	/**
	 * The plug-in vendor. May be <code>null</code>.
	 */
	private String vendor;

	/**
	 * Location of this plug-in.
	 */
	private String location;

	/**
	 * When not <code>null</code>, a new version of this plug-in is available
	 */
	private String newVersion;

	/**
	 * When not <code>null</code>, corresponds to the version activated on the next reload.
	 */
	private String latestLocalVersion;

	/**
	 * When <code>true</code> is plug-in is locally deleted, and will be uninstalled on the next reload.
	 */
	private boolean deleted;
}
