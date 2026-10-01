/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.crypto.WebAuthnHelper;

/**
 * Test class of {@link FakeAuthenticator}: its registrations and assertions are valid for {@link WebAuthnHelper}.
 */
class FakeAuthenticatorTest {

	@Test
	void registrationAndAssertion() {
		final var authenticator = new FakeAuthenticator(5);
		authenticator.setAaguid("00000000-0000-0000-0000-000000000001");
		final var registration = WebAuthnHelper.parseAttestationObject(
				WebAuthnHelper.base64UrlDecode(authenticator.attestationObject("localhost", WebAuthnHelper.FLAG_UP)));
		Assertions.assertTrue(registration.has(WebAuthnHelper.FLAG_AT));
		Assertions.assertEquals(5, registration.signCount());
		Assertions.assertEquals(authenticator.getCredentialId(), WebAuthnHelper.base64Url(registration.credentialId()));
		Assertions.assertEquals(1, registration.aaguid()[15]);
		final var credential = WebAuthnHelper.toCredential(registration.cosePublicKey());

		authenticator.setCounter(6);
		Assertions.assertEquals(6, authenticator.getCounter());
		final var clientData = FakeAuthenticator.clientData("webauthn.get", "challenge", "https://localhost");
		final var assertion = authenticator.assertion("localhost", WebAuthnHelper.FLAG_UP, clientData);
		final var authData = WebAuthnHelper.base64UrlDecode(assertion.authenticatorData());
		Assertions.assertEquals(6, WebAuthnHelper.parseAuthData(authData).signCount());
		Assertions.assertTrue(WebAuthnHelper.verifySignature(credential.publicKey(), credential.alg(), authData,
				WebAuthnHelper.base64UrlDecode(clientData), WebAuthnHelper.base64UrlDecode(assertion.signature())));
	}

	@Test
	void fixed() {
		// Sign byte of BigInteger.toByteArray() removed, short values left-padded
		final var signed = new byte[33];
		signed[1] = (byte) 0x80;
		Assertions.assertEquals((byte) 0x80, FakeAuthenticator.fixed(signed)[0]);
		Assertions.assertEquals(1, FakeAuthenticator.fixed(new byte[] { 1 })[31]);
		Assertions.assertEquals(2, FakeAuthenticator.fixed(new byte[] { 1, 2 })[31]);
		Assertions.assertEquals(32, FakeAuthenticator.fixed(new byte[] { 0 }).length);
	}

	@Test
	void unknownAlgorithms() {
		Assertions.assertThrows(IllegalStateException.class, () -> new FakeAuthenticator(0, "any", "SHA256withECDSA"));
		final var authenticator = new FakeAuthenticator(0, "EC", "any");
		final var clientData = FakeAuthenticator.clientData("webauthn.get", "challenge", "https://localhost");
		Assertions.assertThrows(IllegalStateException.class, () -> authenticator.assertion("localhost", 0, clientData));
	}
}
