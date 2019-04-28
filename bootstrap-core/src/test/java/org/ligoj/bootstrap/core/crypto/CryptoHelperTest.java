/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * {@link CryptoHelper} test class.
 */
class CryptoHelperTest {

	/**
	 * Test encrypt.
	 */
	@Test
    void encryptAsNeeded() {
		final var stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("value")).thenThrow(new EncryptionOperationNotPossibleException());
		Mockito.when(stringEncryptor.encrypt("value")).thenReturn("encrypted");
		final var securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("encrypted", securityHelper.encryptAsNeeded("value"));
	}

	/**
	 * Test encrypt an encrypted value.
	 */
	@Test
    void encryptAsNeededAlreadyEncrypted() {
		final var stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("encrypted")).thenReturn("value");
		final var securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("encrypted", securityHelper.encryptAsNeeded("encrypted"));
	}

	/**
	 * Test decrypt.
	 */
	@Test
    void decryptAsNeeded() {
		final var stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("encrypted")).thenReturn("value");
		final var securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("value", securityHelper.decryptAsNeeded("encrypted"));
	}

	/**
	 * Test decrypt.
	 */
	@Test
    void decryptedOnlyWasNotSecured() {
		final var stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("value")).thenThrow(new EncryptionOperationNotPossibleException());
		final var securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("value", securityHelper.decryptedOnly("value"));
	}

	/**
	 * Test decrypt forbidden.
	 */
	@Test
    void decryptedOnlyWasSecured() {
		final var stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("encrypted")).thenReturn("value");
		final var securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertNull(securityHelper.decryptedOnly("encrypted"));
	}

	/**
	 * Test decrypt forbidden.
	 */
	@Test
    void decryptedOnlyNull() {
		final var securityHelper = new CryptoHelper();
		Assertions.assertEquals("  ", securityHelper.decryptedOnly("  "));
	}

	/**
	 * Test decrypt a raw value.
	 */
	@Test
    void decryptAsNeededAlreadyDecrypted() {
		final var stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("value")).thenThrow(new EncryptionOperationNotPossibleException());
		final var securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("value", securityHelper.decryptAsNeeded("value"));
	}

}
