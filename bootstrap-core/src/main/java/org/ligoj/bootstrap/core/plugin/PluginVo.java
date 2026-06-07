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
	 * The plug-in vendor. May be <code>null</code>. This value is self-declared by the plug-in
	 * (<code>Implementation-Vendor</code> manifest attribute), see {@link #signature} for the verified identity.
	 */
	private String vendor;

	/**
	 * The code signature state of the installed plug-in JAR, computed at startup. <code>null</code> for not locally
	 * installed plug-ins. The {@link PluginSignature#signer()} holds the certificate identity, trustable only with
	 * the {@link PluginSignature.Status#VERIFIED} status.
	 */
	private PluginSignature signature;

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
