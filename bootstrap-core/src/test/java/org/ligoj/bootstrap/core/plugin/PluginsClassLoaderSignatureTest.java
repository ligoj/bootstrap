/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Code-signature tests of {@link PluginsClassLoader}: fixtures hold a signed, an unsigned and a tampered (altered
 * after signature) plug-in JAR, plus a truststore pinning the signer certificate and another, unrelated, truststore.
 */
class PluginsClassLoaderSignatureTest {

	private static final String HOME = "target/test-classes/home-test-signature/.ligoj";
	private static final String SECURITY = "target/test-classes/security-signature";
	private static final String SIGNER_DN = "CN=Ligoj Test Vendor,O=Ligoj,C=FR";

	@AfterEach
	void cleanProperties() {
		System.clearProperty(PluginsClassLoader.HOME_DIR_PROPERTY);
		System.clearProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY);
		System.clearProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PASSWORD_PROPERTY);
		System.clearProperty(PluginsClassLoader.SIGNATURE_REQUIRED_PROPERTY);
	}

	private PluginsClassLoader newClassLoader() throws IOException, NoSuchAlgorithmException {
		return newClassLoader(HOME);
	}

	private PluginsClassLoader newClassLoader(final String home) throws IOException, NoSuchAlgorithmException {
		System.setProperty(PluginsClassLoader.HOME_DIR_PROPERTY, home);
		try (var classLoader = new PluginsClassLoader()) {
			return classLoader;
		}
	}

	private boolean inClasspath(final PluginsClassLoader classLoader, final String artifact) {
		return Arrays.stream(classLoader.getURLs()).map(URL::toString).anyMatch(u -> u.contains(artifact));
	}

	@Test
	void signaturesWithoutTrustStore() throws Exception {
		final var classLoader = newClassLoader();
		final var signatures = classLoader.getSignatures();

		// Valid signature, but no truststore: signer displayed, not "verified"
		Assertions.assertEquals(PluginSignature.Status.SIGNED, signatures.get("plugin-signed").status());
		Assertions.assertEquals(SIGNER_DN, signatures.get("plugin-signed").signer());

		// No signature at all
		Assertions.assertEquals(PluginSignature.Status.UNSIGNED, signatures.get("plugin-unsigned").status());
		Assertions.assertNull(signatures.get("plugin-unsigned").signer());

		// Content altered after the signature
		Assertions.assertEquals(PluginSignature.Status.INVALID, signatures.get("plugin-tampered").status());

		// Not in "required" mode: all plug-ins joined the classpath
		Assertions.assertTrue(inClasspath(classLoader, "plugin-signed"));
		Assertions.assertTrue(inClasspath(classLoader, "plugin-unsigned"));
		Assertions.assertTrue(inClasspath(classLoader, "plugin-tampered"));
	}

	@Test
	void signaturesWithTrustStore() throws Exception {
		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + "/truststore.p12");
		final var signatures = newClassLoader().getSignatures();

		// The signer certificate is pinned in the truststore
		Assertions.assertEquals(PluginSignature.Status.VERIFIED, signatures.get("plugin-signed").status());
		Assertions.assertEquals(SIGNER_DN, signatures.get("plugin-signed").signer());
		Assertions.assertEquals(PluginSignature.Status.UNSIGNED, signatures.get("plugin-unsigned").status());
		Assertions.assertEquals(PluginSignature.Status.INVALID, signatures.get("plugin-tampered").status());
	}

	@Test
	void signaturesWithDefaultTrustStoreLocation() throws Exception {
		// No property: the truststore is read from the default `code-signing.p12` file inside the home directory
		final var signatures = newClassLoader("target/test-classes/home-test-signature-default/.ligoj")
				.getSignatures();
		Assertions.assertEquals(PluginSignature.Status.VERIFIED, signatures.get("plugin-signed").status());
		Assertions.assertEquals(SIGNER_DN, signatures.get("plugin-signed").signer());
	}

	@Test
	void signaturesWithUnrelatedTrustStore() throws Exception {
		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + "/truststore-other.p12");
		final var signatures = newClassLoader().getSignatures();

		// Valid signature, untrusted signer: stays "signed"
		Assertions.assertEquals(PluginSignature.Status.SIGNED, signatures.get("plugin-signed").status());
	}

	@Test
	void signaturesRequired() throws Exception {
		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + "/truststore.p12");
		System.setProperty(PluginsClassLoader.SIGNATURE_REQUIRED_PROPERTY, "true");
		final var classLoader = newClassLoader();

		// Only the verified plug-in joined the classpath, the signature report stays complete
		Assertions.assertTrue(inClasspath(classLoader, "plugin-signed"));
		Assertions.assertFalse(inClasspath(classLoader, "plugin-unsigned"));
		Assertions.assertFalse(inClasspath(classLoader, "plugin-tampered"));
		Assertions.assertEquals(PluginSignature.Status.VERIFIED, classLoader.getSignatures().get("plugin-signed").status());
		Assertions.assertEquals(PluginSignature.Status.UNSIGNED, classLoader.getSignatures().get("plugin-unsigned").status());
	}

	@Test
	void signaturesRequiredWithoutTrustStore() throws Exception {
		System.setProperty(PluginsClassLoader.SIGNATURE_REQUIRED_PROPERTY, "true");
		final var classLoader = newClassLoader();

		// Without truststore, the bar is "signed": the unsigned and tampered plug-ins are excluded
		Assertions.assertTrue(inClasspath(classLoader, "plugin-signed"));
		Assertions.assertFalse(inClasspath(classLoader, "plugin-unsigned"));
		Assertions.assertFalse(inClasspath(classLoader, "plugin-tampered"));
	}

	@Test
	void signaturesUnreadableTrustStore() throws Exception {
		// Unreadable truststore: degrades to the no-truststore behavior
		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + "/not-existing.p12");
		final var signatures = newClassLoader().getSignatures();
		Assertions.assertEquals(PluginSignature.Status.SIGNED, signatures.get("plugin-signed").status());
	}
}
