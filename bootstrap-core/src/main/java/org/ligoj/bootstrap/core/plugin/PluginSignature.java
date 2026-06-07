/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

/**
 * Code signature state of an installed plug-in JAR, computed at startup by the {@link PluginsClassLoader}.
 *
 * @param status The signature status of the JAR.
 * @param signer The signer distinguished name (RFC2253 subject DN of the signing certificate). <code>null</code> when
 *               the JAR is not signed.
 */
public record PluginSignature(Status status, String signer) {

	/**
	 * Code signature status of a plug-in JAR.
	 */
	public enum Status {

		/**
		 * No code signature at all.
		 */
		UNSIGNED,

		/**
		 * Broken signature: digest mismatch (tampered content), partially signed entries or unreadable archive.
		 */
		INVALID,

		/**
		 * Valid and complete signature, but the signing certificate does not chain to the configured truststore — or
		 * no truststore is configured.
		 */
		SIGNED,

		/**
		 * Valid and complete signature whose certificate chains to the configured truststore: the signer identity is
		 * trusted.
		 */
		VERIFIED
	}
}
