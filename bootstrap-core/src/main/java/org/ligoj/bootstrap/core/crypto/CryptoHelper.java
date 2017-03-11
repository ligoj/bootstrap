package org.ligoj.bootstrap.core.crypto;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;

/**
 * Utility managing {@link StringEncryptor}
 */
public final class CryptoHelper {

	@Autowired
	@Setter
	private StringEncryptor encryptor;

	/**
	 * Decrypt a potentially encrypted value.
	 * 
	 * @param value
	 *            The encrypted value to decrypt if not <code>null</code>.
	 * @return the decrypted value.
	 */
	public String decrypt(final String value) {
		return encryptor.decrypt(value);
	}

	/**
	 * Decrypt a potentially encrypted value.
	 * 
	 * @param value
	 *            The encrypted value to decrypt if not <code>null</code>.
	 * @return the decrypted value.
	 */
	public String decryptAsNeeded(final String value) {
		try {
			// Try a decryption
			return decrypt(value);
		} catch (final EncryptionOperationNotPossibleException e) {
			// Value could be encrypted, but was not
			return value;
		}
	}

	/**
	 * Encrypt a clear value.
	 * 
	 * @param value
	 *            A raw value to encrypt.
	 * @return The encrypted value.
	 */
	public String encrypt(final String value) {
		return encryptor.encrypt(value);
	}

	/**
	 * Encrypt a clear value. Try to decrypt the value, and if succeed, return the formal parameter without encrypting
	 * again the value.
	 * 
	 * @param value
	 *            A potentially raw value to encrypt.
	 * @return The encrypted value, or formal parameter if was already encrypted.
	 */
	public String encryptAsNeeded(final String value) {
		try {
			// Try a decryption
			decrypt(value);

			// Value is already encrypted
			return value;
		} catch (final EncryptionOperationNotPossibleException e) {
			// Value could be encrypted, but was not
			return encrypt(value);
		}
	}
}
