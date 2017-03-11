package org.ligoj.bootstrap.core.crypto;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.Assert;
import org.junit.Test;
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
		Assert.assertEquals("encrypted", securityHelper.encryptAsNeeded("value"));
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
		Assert.assertEquals("encrypted", securityHelper.encryptAsNeeded("encrypted"));
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
		Assert.assertEquals("value", securityHelper.decryptAsNeeded("encrypted"));
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
		Assert.assertEquals("value", securityHelper.decryptAsNeeded("value"));
	}

}
