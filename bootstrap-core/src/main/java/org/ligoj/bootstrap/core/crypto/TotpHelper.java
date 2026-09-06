package org.ligoj.bootstrap.core.crypto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.experimental.UtilityClass;

/**
 * Time-based one-time password (RFC 6238, HMAC-SHA1, 6 digits, 30 seconds), compatible with the common authenticator
 * applications. Secrets are Base32 (RFC 4648, no padding) strings, as expected by the <code>otpauth</code> URIs.
 */
@UtilityClass
public class TotpHelper {

	/**
	 * Number of digits of a code.
	 */
	public static final int DIGITS = 6;

	/**
	 * Duration of a time step, in seconds.
	 */
	public static final int PERIOD = 30;

	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
	private static final int SECRET_BYTES = 20;
	private static final int MODULO = 1_000_000;
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Generate a new random secret (160 bits) encoded in Base32.
	 *
	 * @return The Base32 secret, without padding.
	 */
	public static String generateSecret() {
		final var bytes = new byte[SECRET_BYTES];
		RANDOM.nextBytes(bytes);
		return base32(bytes);
	}

	/**
	 * Encode bytes in Base32 (RFC 4648), without padding.
	 *
	 * @param data The bytes to encode.
	 * @return The Base32 string.
	 */
	public static String base32(final byte[] data) {
		final var out = new StringBuilder((data.length * 8 + 4) / 5);
		var buffer = 0;
		var bits = 0;
		for (final var b : data) {
			buffer = (buffer << 8) | (b & 0xFF);
			bits += 8;
			while (bits >= 5) {
				out.append(ALPHABET.charAt((buffer >> (bits - 5)) & 0x1F));
				bits -= 5;
			}
		}
		if (bits > 0) {
			out.append(ALPHABET.charAt((buffer << (5 - bits)) & 0x1F));
		}
		return out.toString();
	}

	/**
	 * Decode a Base32 string (RFC 4648). Case-insensitive, padding, spaces and dashes are ignored.
	 *
	 * @param base32 The Base32 string.
	 * @return The decoded bytes.
	 * @throws IllegalArgumentException When a character is not in the Base32 alphabet.
	 */
	public static byte[] base32Decode(final String base32) {
		final var clean = base32.toUpperCase().replaceAll("[=\\s-]", "");
		final var out = new byte[clean.length() * 5 / 8];
		var buffer = 0;
		var bits = 0;
		var index = 0;
		for (final var c : clean.toCharArray()) {
			final var value = ALPHABET.indexOf(c);
			if (value < 0) {
				throw new IllegalArgumentException("Invalid Base32 character");
			}
			buffer = (buffer << 5) | value;
			bits += 5;
			if (bits >= 8) {
				out[index++] = (byte) ((buffer >> (bits - 8)) & 0xFF);
				bits -= 8;
			}
		}
		return out;
	}

	/**
	 * Compute the code of a counter (HOTP, RFC 4226).
	 *
	 * @param secret  The Base32 secret.
	 * @param counter The counter, i.e. the time step for TOTP.
	 * @return The zero-padded {@value #DIGITS} digits code.
	 */
	// HMAC-SHA1 is the algorithm mandated by RFC 4226 and the default of RFC 6238: SHA-1 is used as a keyed MAC over
	// an 8-byte counter, a construction not affected by the SHA-1 collision weakness, and it is the only algorithm
	// every authenticator application (Google Authenticator, ...) honours in the otpauth URI. SHA-256/512 would break
	// the enrolled devices without a security gain for 6-digit codes verified server-side with an attempt limit.
	@SuppressWarnings("java:S4790")
	public static String code(final String secret, final long counter) {
		try {
			final var mac = Mac.getInstance("HmacSHA1");
			mac.init(new SecretKeySpec(base32Decode(secret), "HmacSHA1"));
			final var message = new byte[8];
			var value = counter;
			for (var i = 7; i >= 0; i--) {
				message[i] = (byte) (value & 0xFF);
				value >>>= 8;
			}
			final var hash = mac.doFinal(message);
			final var offset = hash[hash.length - 1] & 0x0F;
			final var binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
					| ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);
			return String.format("%0" + DIGITS + "d", binary % MODULO);
		} catch (final java.security.GeneralSecurityException e) {
			throw new IllegalStateException("HmacSHA1 is not available", e);
		}
	}

	/**
	 * Current time step.
	 *
	 * @return The current counter.
	 */
	public static long currentCounter() {
		return Instant.now().getEpochSecond() / PERIOD;
	}

	/**
	 * Verify a code against the current time, accepting the adjacent time steps within the window.
	 *
	 * @param secret The Base32 secret.
	 * @param code   The code typed by the user, spaces ignored.
	 * @param window The number of time steps accepted before and after the current one.
	 * @return <code>true</code> when the code matches.
	 */
	public static boolean verify(final String secret, final String code, final int window) {
		return verify(secret, code, currentCounter(), window);
	}

	/**
	 * Verify a code against a counter, accepting the adjacent counters within the window. Constant time comparison.
	 *
	 * @param secret  The Base32 secret.
	 * @param code    The code typed by the user, spaces ignored.
	 * @param counter The reference counter.
	 * @param window  The number of counters accepted before and after the reference.
	 * @return <code>true</code> when the code matches.
	 */
	public static boolean verify(final String secret, final String code, final long counter, final int window) {
		if (code == null) {
			return false;
		}
		final var typed = code.replaceAll("\\s", "").getBytes(StandardCharsets.US_ASCII);
		var match = false;
		for (var i = -window; i <= window; i++) {
			// No early return: constant time over the window
			match |= MessageDigest.isEqual(code(secret, counter + i).getBytes(StandardCharsets.US_ASCII), typed);
		}
		return match;
	}

	/**
	 * Build the <code>otpauth</code> URI used by the authenticator applications (QR code content).
	 *
	 * @param issuer  The issuer, such as the application name.
	 * @param account The account, such as the user login.
	 * @param secret  The Base32 secret.
	 * @return The <code>otpauth://totp/...</code> URI.
	 */
	public static String toUri(final String issuer, final String account, final String secret) {
		final var encodedIssuer = encode(issuer);
		return "otpauth://totp/" + encodedIssuer + ":" + encode(account) + "?secret=" + secret + "&issuer=" + encodedIssuer
				+ "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD;
	}

	private static String encode(final String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}
}
