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
public class CryptoHelperTest {

	/**
	 * Test encrypt.
	 */
	@Test
	public void encryptAsNeeded() {
		final StringEncryptor stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("value")).thenThrow(new EncryptionOperationNotPossibleException());
		Mockito.when(stringEncryptor.encrypt("value")).thenReturn("encrypted");
		final CryptoHelper securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("encrypted", securityHelper.encryptAsNeeded("value"));
	}

	/**
	 * Test encrypt an encrypted value.
	 */
	@Test
	public void encryptAsNeededAlreadyEncrypted() {
		final StringEncryptor stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("encrypted")).thenReturn("value");
		final CryptoHelper securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("encrypted", securityHelper.encryptAsNeeded("encrypted"));
	}

	/**
	 * Test decrypt.
	 */
	@Test
	public void decryptAsNeeded() {
		final StringEncryptor stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("encrypted")).thenReturn("value");
		final CryptoHelper securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("value", securityHelper.decryptAsNeeded("encrypted"));
	}

	/**
	 * Test decrypt.
	 */
	@Test
	public void decryptedOnlyWasNotSecured() {
		final StringEncryptor stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("value")).thenThrow(new EncryptionOperationNotPossibleException());
		final CryptoHelper securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("value", securityHelper.decryptedOnly("value"));
	}

	/**
	 * Test decrypt forbidden.
	 */
	@Test
	public void decryptedOnlyWasSecured() {
		final StringEncryptor stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("encrypted")).thenReturn("value");
		final CryptoHelper securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertNull(securityHelper.decryptedOnly("encrypted"));
	}

	/**
	 * Test decrypt forbidden.
	 */
	@Test
	public void decryptedOnlyNull() {
		final CryptoHelper securityHelper = new CryptoHelper();
		Assertions.assertEquals("  ", securityHelper.decryptedOnly("  "));
	}

	/**
	 * Test decrypt a raw value.
	 */
	@Test
	public void decryptAsNeededAlreadyDecrypted() {
		final StringEncryptor stringEncryptor = Mockito.mock(StringEncryptor.class);
		Mockito.when(stringEncryptor.decrypt("value")).thenThrow(new EncryptionOperationNotPossibleException());
		final CryptoHelper securityHelper = new CryptoHelper();
		securityHelper.setEncryptor(stringEncryptor);
		Assertions.assertEquals("value", securityHelper.decryptAsNeeded("value"));
	}

}
