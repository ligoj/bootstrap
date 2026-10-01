/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.plugin;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

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
		try (var cl = newClassLoader()) {
			var signatures = cl.getSignatures();

			// The signer certificate is pinned in the truststore
			Assertions.assertEquals(PluginSignature.Status.VERIFIED, signatures.get("plugin-signed").status());
			Assertions.assertEquals(SIGNER_DN, signatures.get("plugin-signed").signer());
			Assertions.assertEquals(PluginSignature.Status.UNSIGNED, signatures.get("plugin-unsigned").status());
			Assertions.assertEquals(PluginSignature.Status.INVALID, signatures.get("plugin-tampered").status());
		}
	}

	@Test
	void signaturesWithDefaultTrustStoreLocation() throws Exception {
		// No property: the truststore is read from the default `code-signing.p12` file inside the home directory
		try (var cl = newClassLoader("target/test-classes/home-test-signature-default/.ligoj")) {
			var signatures = cl.getSignatures();
			Assertions.assertEquals(PluginSignature.Status.VERIFIED, signatures.get("plugin-signed").status());
			Assertions.assertEquals(SIGNER_DN, signatures.get("plugin-signed").signer());
		}
	}

	@Test
	void signaturesWithUnrelatedTrustStore() throws Exception {
		// Valid signature, untrusted signer: stays "signed"
		assertSigned("/truststore-other.p12");
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

		// Without truststore, nothing can be verified: fail closed, even the signed plug-in is excluded
		Assertions.assertEquals(PluginSignature.Status.SIGNED, classLoader.getSignatures().get("plugin-signed").status());
		Assertions.assertFalse(inClasspath(classLoader, "plugin-signed"));
		Assertions.assertFalse(inClasspath(classLoader, "plugin-unsigned"));
		Assertions.assertFalse(inClasspath(classLoader, "plugin-tampered"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "/not-existing.p12", "/corrupt.p12" })
	void signaturesRequiredUnreadableTrustStore(final String p12) throws Exception {
		// A configured but unusable truststore must not lower the bar to "signed": fail closed
		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + p12);
		System.setProperty(PluginsClassLoader.SIGNATURE_REQUIRED_PROPERTY, "true");
		final var classLoader = newClassLoader();
		Assertions.assertFalse(inClasspath(classLoader, "plugin-signed"));
		Assertions.assertFalse(inClasspath(classLoader, "plugin-unsigned"));
	}

	@Test
	void signaturesUnreadableTrustStore() throws Exception {
		// Unreadable truststore: degrades to the no-truststore behavior
		assertSigned("/not-existing.p12");
	}

	@Test
	void signaturesCorruptTrustStore() throws Exception {
		// A file that is not a keystore: degrades to the no-truststore behavior
		assertSigned("/corrupt.p12");
	}

	private void assertSigned(String p12) throws Exception {
		// A file that is not a keystore: degrades to the no-truststore behavior
		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + p12);
		try (var cl = newClassLoader()) {
			var signatures = cl.getSignatures();
			Assertions.assertEquals(PluginSignature.Status.SIGNED, signatures.get("plugin-signed").status());
		}
	}

	@Test
	void signaturesPartiallySigned() throws Exception {
		// Signed JAR with content appended after the signature (unsigned entries in and
		// outside META-INF): as unsafe as a tampered one. The exotic signature-block
		// extensions (.DSA/.EC) are tolerated as such.
		try (var cl = newClassLoader()) {
			var signatures = cl.getSignatures();
			Assertions.assertEquals(PluginSignature.Status.INVALID, signatures.get("plugin-partial").status());
			Assertions.assertEquals(SIGNER_DN, signatures.get("plugin-partial").signer());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "META-INF/spring/business-context-common.xml", "META-INF/spring/x.RSA" })
	void signaturesUnsignedMetaInfContent(final String entry) throws Exception {
		// Signed and trusted JAR with an unsigned META-INF entry appended after the signature. META-INF holds
		// executable content (Spring contexts imported with "classpath*:", services, multi-release classes,...):
		// only the manifest and the signature files directly in META-INF may stay unsigned.
		final var home = Path.of("target/test-classes/home-test-signature-meta/.ligoj");
		final var plugin = home.resolve("plugins/plugin-meta-1.0.0.jar");
		Files.createDirectories(plugin.getParent());
		Files.copy(Path.of(HOME, "plugins/plugin-signed-1.0.0.jar"), plugin, StandardCopyOption.REPLACE_EXISTING);
		try (var zip = FileSystems.newFileSystem(plugin)) {
			final var content = zip.getPath(entry);
			Files.createDirectories(content.getParent());
			Files.writeString(content, "<beans/>");
		}

		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + "/truststore.p12");
		System.setProperty(PluginsClassLoader.SIGNATURE_REQUIRED_PROPERTY, "true");
		final var classLoader = newClassLoader(home.toString());
		Assertions.assertEquals(PluginSignature.Status.INVALID, classLoader.getSignatures().get("plugin-meta").status());
		Assertions.assertEquals(SIGNER_DN, classLoader.getSignatures().get("plugin-meta").signer());
		Assertions.assertFalse(inClasspath(classLoader, "plugin-meta"));
	}

	/**
	 * Run a JDK tool, such as "keytool" or "jarsigner".
	 */
	private void jdkTool(final String... command) throws Exception {
		final var tool = Path.of(System.getProperty("java.home"), "bin", command[0]).toString();
		command[0] = tool;
		final var process = new ProcessBuilder(command).redirectErrorStream(true).start();
		final var output = new String(process.getInputStream().readAllBytes());
		Assertions.assertEquals(0, process.waitFor(), output);
	}

	@ParameterizedTest
	@CsvSource({ "EKU=serverAuth,SIGNED", "BC=ca:true,SIGNED", "EKU=codeSigning,VERIFIED", "EKU=anyExtendedKeyUsage,VERIFIED" })
	void signaturesCodeSigningCertificate(final String extension, final PluginSignature.Status expected) throws Exception {
		// A trusted certificate not intended for code signing (TLS server, CA) cannot verify a plug-in
		final var home = Path.of("target/test-classes/home-test-signature-usage/.ligoj");
		FileUtils.deleteDirectory(home.toFile());
		final var plugin = home.resolve("plugins/plugin-usage-1.0.0.jar");
		Files.createDirectories(plugin.getParent());
		Files.copy(Path.of(HOME, "plugins/plugin-unsigned-1.0.0.jar"), plugin);
		final var keystore = home.resolve("signer.p12").toString();
		final var truststore = home.resolve("truststore.p12").toString();
		final var certificate = home.resolve("signer.cer").toString();
		jdkTool("keytool", "-genkeypair", "-keystore", keystore, "-storetype", "PKCS12", "-storepass", "changeit",
				"-alias", "signer", "-keyalg", "RSA", "-keysize", "2048", "-dname", "CN=Not a code signer", "-ext", extension);
		jdkTool("keytool", "-exportcert", "-keystore", keystore, "-storepass", "changeit", "-alias", "signer", "-file", certificate);
		jdkTool("keytool", "-importcert", "-noprompt", "-keystore", truststore, "-storetype", "PKCS12", "-storepass",
				"changeit", "-alias", "signer", "-file", certificate);
		jdkTool("jarsigner", "-keystore", keystore, "-storepass", "changeit", plugin.toString(), "signer");

		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, truststore);
		try (var cl = newClassLoader(home.toString())) {
			Assertions.assertEquals(expected, cl.getSignatures().get("plugin-usage").status());
		}
	}

	@Test
	void isCodeSigningCertificateInvalidUsage() throws Exception {
		final var certificate = Mockito.mock(X509Certificate.class);
		Mockito.when(certificate.getBasicConstraints()).thenReturn(-1);
		Mockito.when(certificate.getExtendedKeyUsage()).thenThrow(new CertificateParsingException("invalid"));
		Assertions.assertFalse(PluginsClassLoader.isCodeSigningCertificate(certificate));
	}

	/**
	 * Copy a plug-in of the default home to a new home, and update its entries.
	 */
	private Path copyPlugin(final String home, final String source, final String target,
			final java.util.function.Consumer<java.nio.file.FileSystem> update) throws IOException {
		final var homePath = Path.of(home);
		FileUtils.deleteDirectory(homePath.toFile());
		final var plugin = homePath.resolve("plugins/" + target);
		Files.createDirectories(plugin.getParent());
		Files.copy(Path.of(HOME, "plugins/" + source), plugin);
		try (var zip = FileSystems.newFileSystem(plugin)) {
			update.accept(zip);
		}
		return homePath;
	}

	@Test
	void signaturesNoSignedContent() throws Exception {
		// Signature files, but no signed content entry
		final var home = copyPlugin("target/test-classes/home-test-signature-empty/.ligoj", "plugin-signed-1.0.0.jar",
				"plugin-empty-1.0.0.jar", zip -> {
					try {
						Files.delete(zip.getPath("config.properties"));
						Files.delete(zip.getPath("org/ligoj/test/dummy.txt"));
					} catch (final IOException e) {
						throw new IllegalStateException(e);
					}
				});
		try (var cl = newClassLoader(home.toString())) {
			Assertions.assertEquals(PluginSignature.Status.UNSIGNED, cl.getSignatures().get("plugin-empty").status());
		}
	}

	@Test
	void signaturesSignatureFileInSubdirectory() throws Exception {
		// A ".SF" entry outside the META-INF directory itself is not a signature file
		final var home = copyPlugin("target/test-classes/home-test-signature-sub/.ligoj", "plugin-unsigned-1.0.0.jar",
				"plugin-sub-1.0.0.jar", zip -> {
					try {
						final var file = zip.getPath("META-INF/sub/x.SF");
						Files.createDirectories(file.getParent());
						Files.writeString(file, "Signature-Version: 1.0");
					} catch (final IOException e) {
						throw new IllegalStateException(e);
					}
				});
		try (var cl = newClassLoader(home.toString())) {
			Assertions.assertEquals(PluginSignature.Status.UNSIGNED, cl.getSignatures().get("plugin-sub").status());
		}
	}

	@Test
	void signaturesChainTrustStoreJks() throws Exception {
		// The signer certificate is NOT pinned, but its issuing CA is in the (JKS) truststore:
		// the certificate path validates (PKIX) and the plug-in is VERIFIED
		System.setProperty(PluginsClassLoader.SIGNATURE_TRUSTSTORE_PROPERTY, SECURITY + "/truststore-ca.jks");
		try (var cl = newClassLoader("target/test-classes/home-test-signature-ca/.ligoj")) {
			var signatures = cl.getSignatures();
			Assertions.assertEquals(PluginSignature.Status.VERIFIED, signatures.get("plugin-chain").status());
			Assertions.assertEquals("CN=Ligoj Chain Vendor,O=Ligoj,C=FR", signatures.get("plugin-chain").signer());
		}
	}

	@Test
	void signaturesUnreadablePlugin() throws Exception {
		// A plug-in file that cannot be read as a JAR: INVALID, and excluded in "required"
		// mode (which also keeps the unreadable file away from the resource-export step)
		System.setProperty(PluginsClassLoader.SIGNATURE_REQUIRED_PROPERTY, "true");
		final var classLoader = newClassLoader("target/test-classes/home-test-signature-broken/.ligoj");
		Assertions.assertEquals(PluginSignature.Status.INVALID, classLoader.getSignatures().get("plugin-garbage").status());
		Assertions.assertFalse(inClasspath(classLoader, "plugin-garbage"));
	}
}
