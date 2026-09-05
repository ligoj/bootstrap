package org.ligoj.bootstrap.core.crypto;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link WebAuthnHelper} and {@link Cbor}, with a synthetic authenticator.
 */
class WebAuthnHelperTest {

	private static byte[] unsigned(final byte[] bytes) {
		// Strip the sign byte of BigInteger.toByteArray()
		return bytes.length > 1 && bytes[0] == 0 ? java.util.Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
	}

	private static byte[] fixed(final byte[] bytes, final int size) {
		final var out = new byte[size];
		final var src = unsigned(bytes);
		System.arraycopy(src, 0, out, size - src.length, src.length);
		return out;
	}

	static byte[] authData(final byte[] rpIdHash, final int flags, final long counter, final byte[] credentialId, final Map<Object, Object> cose) {
		final var out = new ByteArrayOutputStream();
		out.writeBytes(rpIdHash);
		out.write(flags);
		out.write((int) (counter >> 24));
		out.write((int) (counter >> 16));
		out.write((int) (counter >> 8));
		out.write((int) counter);
		if (credentialId != null) {
			out.writeBytes(new byte[16]);
			out.write(credentialId.length >> 8);
			out.write(credentialId.length);
			out.writeBytes(credentialId);
			out.writeBytes(Cbor.encode(cose));
		}
		return out.toByteArray();
	}

	@Test
	void cborRoundTrip() {
		final var map = new LinkedHashMap<Object, Object>();
		map.put("fmt", "none");
		map.put(1L, 2L);
		map.put(-1L, new byte[] { 1, 2, 3 });
		map.put("list", List.of(1L, "a", Boolean.TRUE));
		map.put("big", 70000L);
		map.put("neg", -300L);
		final var decoded = (Map<?, ?>) Cbor.decode(Cbor.encode(map));
		Assertions.assertEquals("none", decoded.get("fmt"));
		Assertions.assertEquals(2L, decoded.get(1L));
		Assertions.assertArrayEquals(new byte[] { 1, 2, 3 }, (byte[]) decoded.get(-1L));
		Assertions.assertEquals(List.of(1L, "a", Boolean.TRUE), decoded.get("list"));
		Assertions.assertEquals(70000L, decoded.get("big"));
		Assertions.assertEquals(-300L, decoded.get("neg"));
		Assertions.assertThrows(IllegalArgumentException.class, () -> Cbor.decode(new byte[] { (byte) 0x5A }));
		Assertions.assertThrows(IllegalArgumentException.class, () -> Cbor.decode(new byte[] { (byte) 0xF9 }));
	}

	@Test
	void es256RegistrationAndAssertion() throws Exception {
		final var generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec("secp256r1"));
		final var pair = generator.generateKeyPair();
		final var ec = (ECPublicKey) pair.getPublic();
		final var cose = new LinkedHashMap<Object, Object>();
		cose.put(1L, 2L);
		cose.put(3L, (long) WebAuthnHelper.ALG_ES256);
		cose.put(-1L, 1L);
		cose.put(-2L, fixed(ec.getW().getAffineX().toByteArray(), 32));
		cose.put(-3L, fixed(ec.getW().getAffineY().toByteArray(), 32));
		final var rpIdHash = WebAuthnHelper.sha256("localhost".getBytes(StandardCharsets.UTF_8));
		final var credentialId = new byte[] { 9, 8, 7, 6, 5 };

		// Registration: attestation object with the credential
		final var registration = new LinkedHashMap<Object, Object>();
		registration.put("fmt", "none");
		registration.put("attStmt", Map.of());
		registration.put("authData", authData(rpIdHash, WebAuthnHelper.FLAG_UP | WebAuthnHelper.FLAG_UV | WebAuthnHelper.FLAG_AT, 0, credentialId, cose));
		final var parsed = WebAuthnHelper.parseAttestationObject(Cbor.encode(registration));
		Assertions.assertArrayEquals(rpIdHash, parsed.rpIdHash());
		Assertions.assertTrue(parsed.has(WebAuthnHelper.FLAG_UP));
		Assertions.assertTrue(parsed.has(WebAuthnHelper.FLAG_UV));
		Assertions.assertArrayEquals(credentialId, parsed.credentialId());
		final var credential = WebAuthnHelper.toCredential(parsed.cosePublicKey());
		Assertions.assertEquals(WebAuthnHelper.ALG_ES256, credential.alg());
		Assertions.assertEquals(ec, credential.publicKey());
		// Persisted form round trip
		Assertions.assertEquals(ec, WebAuthnHelper.decodePublicKey(WebAuthnHelper.encodePublicKey(ec), WebAuthnHelper.ALG_ES256));

		// Assertion: signature over authData || sha256(clientData)
		final var clientData = "{\"type\":\"webauthn.get\",\"challenge\":\"abc\",\"origin\":\"http://localhost:5173\"}".getBytes(StandardCharsets.UTF_8);
		final var assertionData = authData(rpIdHash, WebAuthnHelper.FLAG_UP | WebAuthnHelper.FLAG_UV, 42, null, null);
		final var signer = Signature.getInstance("SHA256withECDSA");
		signer.initSign(pair.getPrivate());
		signer.update(assertionData);
		signer.update(WebAuthnHelper.sha256(clientData));
		final var signature = signer.sign();
		Assertions.assertTrue(WebAuthnHelper.verifySignature(ec, WebAuthnHelper.ALG_ES256, assertionData, clientData, signature));
		Assertions.assertFalse(WebAuthnHelper.verifySignature(ec, WebAuthnHelper.ALG_ES256, assertionData, "{}".getBytes(StandardCharsets.UTF_8), signature));
		Assertions.assertFalse(WebAuthnHelper.verifySignature(ec, WebAuthnHelper.ALG_ES256, assertionData, clientData, new byte[] { 1 }));
		final var parsedAssertion = WebAuthnHelper.parseAuthData(assertionData);
		Assertions.assertEquals(42, parsedAssertion.signCount());
		Assertions.assertNull(parsedAssertion.credentialId());
		final var client = WebAuthnHelper.parseClientData(clientData);
		Assertions.assertEquals("webauthn.get", client.get("type"));
		Assertions.assertEquals("abc", client.get("challenge"));
	}

	@Test
	void rs256() throws Exception {
		final var generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		final var pair = generator.generateKeyPair();
		final var rsa = (RSAPublicKey) pair.getPublic();
		final var cose = new LinkedHashMap<Object, Object>();
		cose.put(1L, 3L);
		cose.put(3L, (long) WebAuthnHelper.ALG_RS256);
		cose.put(-1L, unsigned(rsa.getModulus().toByteArray()));
		cose.put(-2L, unsigned(rsa.getPublicExponent().toByteArray()));
		final var credential = WebAuthnHelper.toCredential(cose);
		Assertions.assertEquals(rsa, credential.publicKey());
		final var data = new byte[] { 1, 2, 3 };
		final var clientData = "{}".getBytes(StandardCharsets.UTF_8);
		final var signer = Signature.getInstance("SHA256withRSA");
		signer.initSign(pair.getPrivate());
		signer.update(data);
		signer.update(WebAuthnHelper.sha256(clientData));
		Assertions.assertTrue(WebAuthnHelper.verifySignature(rsa, WebAuthnHelper.ALG_RS256, data, clientData, signer.sign()));
		Assertions.assertEquals(rsa, WebAuthnHelper.decodePublicKey(WebAuthnHelper.encodePublicKey(rsa), WebAuthnHelper.ALG_RS256));
	}

	@Test
	void unsupportedKeys() {
		final var cose = new LinkedHashMap<Object, Object>();
		cose.put(1L, 2L);
		cose.put(3L, -8L);
		Assertions.assertThrows(IllegalArgumentException.class, () -> WebAuthnHelper.toCredential(cose));
		Assertions.assertThrows(IllegalArgumentException.class, () -> WebAuthnHelper.parseAuthData(new byte[10]));
		Assertions.assertThrows(IllegalArgumentException.class, () -> WebAuthnHelper.parseAttestationObject(Cbor.encode("x")));
		Assertions.assertThrows(IllegalArgumentException.class, () -> WebAuthnHelper.decodePublicKey("AAAA", WebAuthnHelper.ALG_ES256));
	}

	@Test
	void origins() {
		Assertions.assertTrue(WebAuthnHelper.isOriginAllowed("http://localhost:5173", "localhost", List.of()));
		Assertions.assertTrue(WebAuthnHelper.isOriginAllowed("https://ligoj.sample.com", "ligoj.sample.com", null));
		Assertions.assertTrue(WebAuthnHelper.isOriginAllowed("https://app.sample.com", "sample.com", List.of()));
		Assertions.assertFalse(WebAuthnHelper.isOriginAllowed("http://ligoj.sample.com", "ligoj.sample.com", List.of()));
		Assertions.assertFalse(WebAuthnHelper.isOriginAllowed("https://evil.com", "sample.com", List.of()));
		Assertions.assertFalse(WebAuthnHelper.isOriginAllowed(null, "sample.com", List.of()));
		Assertions.assertFalse(WebAuthnHelper.isOriginAllowed("not a uri", "sample.com", List.of()));
		// Explicit list
		Assertions.assertTrue(WebAuthnHelper.isOriginAllowed("https://x.com", "y.com", List.of("https://x.com")));
		Assertions.assertFalse(WebAuthnHelper.isOriginAllowed("https://y.com", "y.com", List.of("https://x.com")));
		Assertions.assertEquals(32, WebAuthnHelper.randomChallenge().length);
		Assertions.assertArrayEquals(new byte[] { 1, 2 }, WebAuthnHelper.base64UrlDecode(WebAuthnHelper.base64Url(new byte[] { 1, 2 })));
	}
}
