package org.ligoj.bootstrap.core.crypto;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

import lombok.experimental.UtilityClass;
import tools.jackson.databind.ObjectMapper;

/**
 * WebAuthn (passkeys) server-side primitives, without attestation statement verification: registration extracts the
 * credential public key from the authenticator data, authentication verifies the assertion signature. Supported
 * algorithms: ES256 (-7) and RS256 (-257).
 */
@UtilityClass
public class WebAuthnHelper {

	/**
	 * COSE algorithm ES256: ECDSA P-256 with SHA-256.
	 */
	public static final int ALG_ES256 = -7;

	/**
	 * COSE algorithm RS256: RSASSA-PKCS1-v1_5 with SHA-256.
	 */
	public static final int ALG_RS256 = -257;

	/**
	 * Authenticator data flag: user present.
	 */
	public static final int FLAG_UP = 0x01;

	/**
	 * Authenticator data flag: user verified.
	 */
	public static final int FLAG_UV = 0x04;

	/**
	 * Authenticator data flag: attested credential data included.
	 */
	public static final int FLAG_AT = 0x40;

	private static final int RP_ID_HASH_LENGTH = 32;
	private static final int AAGUID_LENGTH = 16;
	private static final int CHALLENGE_BYTES = 32;
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * Parsed authenticator data.
	 *
	 * @param rpIdHash      SHA-256 of the relying party identifier.
	 * @param flags         The flags byte, see {@link #FLAG_UP}, {@link #FLAG_UV}, {@link #FLAG_AT}.
	 * @param signCount     The signature counter.
	 * @param aaguid        The authenticator AAGUID, <code>null</code> without attested credential data.
	 * @param credentialId  The credential identifier, <code>null</code> without attested credential data.
	 * @param cosePublicKey The COSE public key (decoded CBOR map), <code>null</code> without attested credential data.
	 */
	public record AuthData(byte[] rpIdHash, int flags, long signCount, byte[] aaguid, byte[] credentialId,
			Map<?, ?> cosePublicKey) {

		/**
		 * @param flag A flag, see {@link #FLAG_UP}.
		 * @return <code>true</code> when the flag is set.
		 */
		public boolean has(final int flag) {
			return (flags & flag) != 0;
		}
	}

	/**
	 * A credential public key.
	 *
	 * @param publicKey The public key.
	 * @param alg       The COSE algorithm.
	 */
	public record Credential(PublicKey publicKey, int alg) {
	}

	/**
	 * A new random challenge.
	 *
	 * @return 32 random bytes.
	 */
	public static byte[] randomChallenge() {
		final var bytes = new byte[CHALLENGE_BYTES];
		RANDOM.nextBytes(bytes);
		return bytes;
	}

	/**
	 * Base64url without padding, as used by WebAuthn JSON.
	 *
	 * @param bytes The bytes.
	 * @return The encoded string.
	 */
	public static String base64Url(final byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	/**
	 * Decode Base64url (padding optional).
	 *
	 * @param value The encoded string.
	 * @return The bytes.
	 */
	public static byte[] base64UrlDecode(final String value) {
		return Base64.getUrlDecoder().decode(value);
	}

	/**
	 * SHA-256 digest.
	 *
	 * @param data The data.
	 * @return The digest.
	 */
	public static byte[] sha256(final byte[] data) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (final GeneralSecurityException e) {
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}

	/**
	 * Parse the client data JSON.
	 *
	 * @param clientDataJSON The raw client data.
	 * @return The JSON object: <code>type</code>, <code>challenge</code>, <code>origin</code>, ...
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseClientData(final byte[] clientDataJSON) {
		return MAPPER.readValue(new String(clientDataJSON, StandardCharsets.UTF_8), Map.class);
	}

	/**
	 * Parse authenticator data: from an assertion (37 bytes), or from a registration (with the attested credential).
	 *
	 * @param authData The authenticator data.
	 * @return The parsed data.
	 */
	public static AuthData parseAuthData(final byte[] authData) {
		if (authData == null || authData.length < RP_ID_HASH_LENGTH + 5) {
			throw new IllegalArgumentException("Truncated authenticator data");
		}
		final var rpIdHash = Arrays.copyOfRange(authData, 0, RP_ID_HASH_LENGTH);
		final var flags = authData[RP_ID_HASH_LENGTH] & 0xFF;
		final var signCount = ((long) (authData[33] & 0xFF) << 24) | ((authData[34] & 0xFF) << 16)
				| ((authData[35] & 0xFF) << 8) | (authData[36] & 0xFF);
		if ((flags & FLAG_AT) == 0) {
			return new AuthData(rpIdHash, flags, signCount, null, null, null);
		}
		var position = 37;
		if (authData.length < position + AAGUID_LENGTH + 2) {
			throw new IllegalArgumentException("Truncated attested credential data");
		}
		final var aaguid = Arrays.copyOfRange(authData, position, position + AAGUID_LENGTH);
		position += AAGUID_LENGTH;
		final var idLength = ((authData[position] & 0xFF) << 8) | (authData[position + 1] & 0xFF);
		position += 2;
		if (authData.length < position + idLength) {
			throw new IllegalArgumentException("Truncated credential identifier");
		}
		final var credentialId = Arrays.copyOfRange(authData, position, position + idLength);
		position += idLength;
		final var key = Cbor.decodeAt(authData, position).value();
		if (!(key instanceof Map<?, ?> cose)) {
			throw new IllegalArgumentException("Invalid COSE public key");
		}
		return new AuthData(rpIdHash, flags, signCount, aaguid, credentialId, cose);
	}

	/**
	 * Parse an attestation object (CBOR map with <code>fmt</code>, <code>attStmt</code>, <code>authData</code>). The
	 * attestation statement is not verified.
	 *
	 * @param attestationObject The raw attestation object.
	 * @return The parsed authenticator data.
	 */
	public static AuthData parseAttestationObject(final byte[] attestationObject) {
		if (!(Cbor.decode(attestationObject) instanceof Map<?, ?> map) || !(map.get("authData") instanceof byte[] authData)) {
			throw new IllegalArgumentException("Invalid attestation object");
		}
		return parseAuthData(authData);
	}

	/**
	 * Convert a COSE public key to a Java public key.
	 *
	 * @param cose The decoded COSE key map.
	 * @return The credential public key and its algorithm.
	 */
	public static Credential toCredential(final Map<?, ?> cose) {
		final var kty = ((Number) cose.get(1L)).intValue();
		final var alg = ((Number) cose.get(3L)).intValue();
		try {
			if (kty == 2 && alg == ALG_ES256) {
				if (((Number) cose.get(-1L)).intValue() != 1) {
					throw new IllegalArgumentException("Unsupported EC curve");
				}
				final var parameters = AlgorithmParameters.getInstance("EC");
				parameters.init(new ECGenParameterSpec("secp256r1"));
				final var point = new ECPoint(new BigInteger(1, (byte[]) cose.get(-2L)), new BigInteger(1, (byte[]) cose.get(-3L)));
				return new Credential(KeyFactory.getInstance("EC")
						.generatePublic(new ECPublicKeySpec(point, parameters.getParameterSpec(ECParameterSpec.class))), alg);
			}
			if (kty == 3 && alg == ALG_RS256) {
				return new Credential(KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(
						new BigInteger(1, (byte[]) cose.get(-1L)), new BigInteger(1, (byte[]) cose.get(-2L)))), alg);
			}
		} catch (final GeneralSecurityException e) {
			throw new IllegalArgumentException("Invalid COSE public key", e);
		}
		throw new IllegalArgumentException("Unsupported COSE key type " + kty + " / algorithm " + alg);
	}

	/**
	 * Encode a public key (X.509 SubjectPublicKeyInfo, Base64).
	 *
	 * @param publicKey The public key.
	 * @return The encoded key.
	 */
	public static String encodePublicKey(final PublicKey publicKey) {
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}

	/**
	 * Decode a public key encoded by {@link #encodePublicKey(PublicKey)}.
	 *
	 * @param encoded The encoded key.
	 * @param alg     The COSE algorithm, telling the key type.
	 * @return The public key.
	 */
	public static PublicKey decodePublicKey(final String encoded, final int alg) {
		try {
			return KeyFactory.getInstance(alg == ALG_RS256 ? "RSA" : "EC")
					.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(encoded)));
		} catch (final GeneralSecurityException e) {
			throw new IllegalArgumentException("Invalid public key", e);
		}
	}

	/**
	 * Verify an assertion signature: over the authenticator data followed by the SHA-256 of the client data.
	 *
	 * @param publicKey      The credential public key.
	 * @param alg            The COSE algorithm.
	 * @param authData       The raw authenticator data.
	 * @param clientDataJSON The raw client data.
	 * @param signature      The signature.
	 * @return <code>true</code> when the signature is valid.
	 */
	public static boolean verifySignature(final PublicKey publicKey, final int alg, final byte[] authData,
			final byte[] clientDataJSON, final byte[] signature) {
		try {
			final var verifier = Signature.getInstance(alg == ALG_RS256 ? "SHA256withRSA" : "SHA256withECDSA");
			verifier.initVerify(publicKey);
			verifier.update(authData);
			verifier.update(sha256(clientDataJSON));
			return verifier.verify(signature);
		} catch (final GeneralSecurityException | IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Whether the client origin is accepted: one of the configured origins when any, otherwise an origin whose host
	 * is the relying party identifier or one of its sub-domains, over HTTPS (HTTP accepted for localhost).
	 *
	 * @param origin  The origin of the client data.
	 * @param rpId    The relying party identifier.
	 * @param allowed The configured origins, may be empty.
	 * @return <code>true</code> when accepted.
	 */
	public static boolean isOriginAllowed(final String origin, final String rpId, final Collection<String> allowed) {
		if (origin == null) {
			return false;
		}
		if (allowed != null && !allowed.isEmpty()) {
			return allowed.stream().anyMatch(a -> a.equalsIgnoreCase(origin));
		}
		try {
			final var uri = URI.create(origin);
			final var host = uri.getHost();
			if (host == null || !(host.equalsIgnoreCase(rpId) || host.toLowerCase().endsWith("." + rpId.toLowerCase()))) {
				return false;
			}
			return "https".equalsIgnoreCase(uri.getScheme())
					|| ("http".equalsIgnoreCase(uri.getScheme()) && "localhost".equalsIgnoreCase(host));
		} catch (final IllegalArgumentException _) {
			return false;
		}
	}
}
