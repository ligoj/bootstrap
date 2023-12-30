/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;

/**
 * Utility managing {@link StringEncryptor}
 */
@Setter
public final class CryptoHelper {

	@Autowired
	private StringEncryptor encryptor;

	/**
	 * Decrypt a potentially encrypted value.
	 *
	 * @param value The encrypted value to decrypt if not <code>null</code>.
	 * @return the decrypted value.
	 */
	public String decrypt(final String value) {
		return encryptor.decrypt(value);
	}

	/**
	 * Decrypt a potentially encrypted value.
	 *
	 * @param value The encrypted value to decrypt if not <code>null</code>.
	 * @return the decrypted value.
	 */
	public String decryptAsNeeded(final String value) {
		try {
			// Try a decryption
			return decrypt(value);
		} catch (final EncryptionOperationNotPossibleException ignored) {
			// This value could be encrypted, but was not
			return value;
		}
	}

	/**
	 * Return the given value only if it is not encrypted. Otherwise, return <code>null</code>.
	 * 
	 * @param value The encrypted (or not) value to check.
	 * @return the raw value only when not encrypted. Otherwise <code>null</code>.
	 */
	public String decryptedOnly(final String value) {
		if (StringUtils.isAllBlank(value)) {
			return value;
		}
		try {
			// Try a decryption
			decrypt(value);
			return null;
		} catch (final EncryptionOperationNotPossibleException e) { // NOSONAR - Ignore raw value
			// Value could be encrypted, consider it as a safe value
			return value;
		}
	}

	/**
	 * Encrypt a clear value.
	 *
	 * @param value A raw value to encrypt.
	 * @return The encrypted value.
	 */
	public String encrypt(final String value) {
		return encryptor.encrypt(value);
	}

	/**
	 * Encrypt a clear value. Try to decrypt the value, and if succeeded, return the formal parameter without encrypting
	 * again the value.
	 *
	 * @param value A potentially raw value to encrypt.
	 * @return The encrypted value, or formal parameter if was already encrypted.
	 */
	public String encryptAsNeeded(final String value) {
		try {
			// Try a decryption
			decrypt(value);

			// Value is already encrypted
			return value;
		} catch (final EncryptionOperationNotPossibleException ignored) {
			// This value could be encrypted, but was not
			return encrypt(value);
		}
	}
}
