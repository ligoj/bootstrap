package org.ligoj.bootstrap.core.crypto;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link TotpHelper}, with the RFC 6238 test vectors (SHA1, 6 last digits).
 */
class TotpHelperTest {

	/**
	 * RFC 6238 shared secret "12345678901234567890" in Base32.
	 */
	private static final String SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

	@Test
	void base32RoundTrip() {
		Assertions.assertEquals(SECRET, TotpHelper.base32("12345678901234567890".getBytes(StandardCharsets.US_ASCII)));
		Assertions.assertEquals("12345678901234567890",
				new String(TotpHelper.base32Decode("gezd gnbv-gy3tqojqgezdgnbvgy3tqojq=="), StandardCharsets.US_ASCII));
		Assertions.assertEquals("", TotpHelper.base32(new byte[0]));
		Assertions.assertEquals("MY", TotpHelper.base32("f".getBytes(StandardCharsets.US_ASCII)));
		Assertions.assertEquals("MZXW6YTBOI", TotpHelper.base32("foobar".getBytes(StandardCharsets.US_ASCII)));
		Assertions.assertThrows(IllegalArgumentException.class, () -> TotpHelper.base32Decode("MZ1"));
	}

	@Test
	void generateSecret() {
		final var secret = TotpHelper.generateSecret();
		Assertions.assertEquals(32, secret.length());
		Assertions.assertEquals(20, TotpHelper.base32Decode(secret).length);
		Assertions.assertNotEquals(secret, TotpHelper.generateSecret());
	}

	@Test
	void codeRfc6238() {
		Assertions.assertEquals("287082", TotpHelper.code(SECRET, 59 / TotpHelper.PERIOD));
		Assertions.assertEquals("081804", TotpHelper.code(SECRET, 1111111109L / TotpHelper.PERIOD));
		Assertions.assertEquals("050471", TotpHelper.code(SECRET, 1111111111L / TotpHelper.PERIOD));
		Assertions.assertEquals("005924", TotpHelper.code(SECRET, 1234567890L / TotpHelper.PERIOD));
		Assertions.assertEquals("279037", TotpHelper.code(SECRET, 2000000000L / TotpHelper.PERIOD));
		Assertions.assertEquals("353130", TotpHelper.code(SECRET, 20000000000L / TotpHelper.PERIOD));
	}

	@Test
	void verifyWindow() {
		final var counter = 1234567890L / TotpHelper.PERIOD;
		Assertions.assertTrue(TotpHelper.verify(SECRET, "005924", counter, 0));
		Assertions.assertTrue(TotpHelper.verify(SECRET, " 005 924 ", counter, 0));
		// Previous and next steps within the window only
		Assertions.assertTrue(TotpHelper.verify(SECRET, TotpHelper.code(SECRET, counter - 1), counter, 1));
		Assertions.assertTrue(TotpHelper.verify(SECRET, TotpHelper.code(SECRET, counter + 1), counter, 1));
		Assertions.assertFalse(TotpHelper.verify(SECRET, TotpHelper.code(SECRET, counter + 2), counter, 1));
		Assertions.assertFalse(TotpHelper.verify(SECRET, "000000", counter, 1));
		Assertions.assertFalse(TotpHelper.verify(SECRET, null, counter, 1));
		// Current time: a freshly computed code is accepted
		Assertions.assertTrue(TotpHelper.verify(SECRET, TotpHelper.code(SECRET, TotpHelper.currentCounter()), 1));
	}

	@Test
	void toUri() {
		Assertions.assertEquals("otpauth://totp/Ligoj%20Dev:john%40sample.com?secret=" + SECRET
				+ "&issuer=Ligoj%20Dev&algorithm=SHA1&digits=6&period=30", TotpHelper.toUri("Ligoj Dev", "john@sample.com", SECRET));
	}
}
