package org.ligoj.bootstrap;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ligoj.bootstrap.core.crypto.Cbor;
import org.ligoj.bootstrap.core.crypto.WebAuthnHelper;

import lombok.Getter;

/**
 * A synthetic ES256 WebAuthn authenticator for the tests: builds registration attestations and assertions the way a
 * browser would return them (Base64url fields).
 */
public class FakeAuthenticator {

	private final KeyPair pair;

	/**
	 * Credential identifier, Base64url.
	 */
	@Getter
	private final String credentialId;

	private final byte[] rawCredentialId;

	/**
	 * Signature counter of the next assertion.
	 */
	@Getter
	private long counter;

	/**
	 * A synthetic ES256 WebAuthn authenticator for the tests.
	 * @param counter Initial signature counter.
	 */
	public FakeAuthenticator(final long counter) {
		try {
			final var generator = KeyPairGenerator.getInstance("EC");
			generator.initialize(new ECGenParameterSpec("secp256r1"));
			pair = generator.generateKeyPair();
		} catch (final GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
		rawCredentialId = WebAuthnHelper.randomChallenge();
		credentialId = WebAuthnHelper.base64Url(rawCredentialId);
		this.counter = counter;
	}

	/**
	 * Client data JSON, Base64url.
	 *
	 * @param type      <code>webauthn.create</code> or <code>webauthn.get</code>.
	 * @param challenge The challenge from the options.
	 * @param origin    The browser origin.
	 * @return The encoded client data.
	 */
	public static String clientData(final String type, final String challenge, final String origin) {
		return WebAuthnHelper.base64Url(("{\"type\":\"" + type + "\",\"challenge\":\"" + challenge + "\",\"origin\":\"" + origin
				+ "\",\"crossOrigin\":false}").getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Attestation object (format <code>none</code>) with this authenticator's credential, Base64url.
	 *
	 * @param rpId  The relying party identifier.
	 * @param flags The authenticator flags, user present and attested credential are added.
	 * @return The encoded attestation object.
	 */
	public String attestationObject(final String rpId, final int flags) {
		final var ec = (ECPublicKey) pair.getPublic();
		final var cose = new LinkedHashMap<Object, Object>();
		cose.put(1L, 2L);
		cose.put(3L, (long) WebAuthnHelper.ALG_ES256);
		cose.put(-1L, 1L);
		cose.put(-2L, fixed(ec.getW().getAffineX().toByteArray()));
		cose.put(-3L, fixed(ec.getW().getAffineY().toByteArray()));
		final var attestation = new LinkedHashMap<Object, Object>();
		attestation.put("fmt", "none");
		attestation.put("attStmt", Map.of());
		attestation.put("authData", authData(rpId, flags | WebAuthnHelper.FLAG_AT, true, cose));
		return WebAuthnHelper.base64Url(Cbor.encode(attestation));
	}

	/**
	 * Sign an assertion: authenticator data and signature, Base64url.
	 *
	 * @param rpId           The relying party identifier.
	 * @param flags          The authenticator flags.
	 * @param clientDataJSON The Base64url client data to sign with.
	 * @return The authenticator data and the signature.
	 */
	public Assertion assertion(final String rpId, final int flags, final String clientDataJSON) {
		final var authData = authData(rpId, flags, false, null);
		try {
			final var signer = Signature.getInstance("SHA256withECDSA");
			signer.initSign(pair.getPrivate());
			signer.update(authData);
			signer.update(WebAuthnHelper.sha256(WebAuthnHelper.base64UrlDecode(clientDataJSON)));
			return new Assertion(WebAuthnHelper.base64Url(authData), WebAuthnHelper.base64Url(signer.sign()));
		} catch (final GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Move the counter forward.
	 *
	 * @param counter The next counter value.
	 */
	public void setCounter(final long counter) {
		this.counter = counter;
	}

	/**
	 * An assertion.
	 *
	 * @param authenticatorData Base64url authenticator data.
	 * @param signature         Base64url signature.
	 */
	public record Assertion(String authenticatorData, String signature) {
	}

	private byte[] authData(final String rpId, final int flags, final boolean attested, final Map<Object, Object> cose) {
		final var out = new ByteArrayOutputStream();
		out.writeBytes(WebAuthnHelper.sha256(rpId.getBytes(StandardCharsets.UTF_8)));
		out.write(flags);
		out.write((int) (counter >> 24));
		out.write((int) (counter >> 16));
		out.write((int) (counter >> 8));
		out.write((int) counter);
		if (attested) {
			out.writeBytes(new byte[16]);
			out.write(rawCredentialId.length >> 8);
			out.write(rawCredentialId.length);
			out.writeBytes(rawCredentialId);
			out.writeBytes(Cbor.encode(cose));
		}
		return out.toByteArray();
	}

	private static byte[] fixed(final byte[] bytes) {
		final var src = bytes.length > 1 && bytes[0] == 0 ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
		final var out = new byte[32];
		System.arraycopy(src, 0, out, 32 - src.length, src.length);
		return out;
	}
}
