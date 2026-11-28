/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Event published during the rotation of the master secret, inside the transaction of this rotation and just before
 * the new secret is used by {@link CryptoHelper}. A listener, such as a method annotated with
 * <code>&#64;EventListener</code>, has to encrypt again the values it owns with {@link #rotate(String)} or
 * {@link #rotateAsNeeded(String)}, and save them in this transaction. A failing listener cancels the rotation.
 * <p>
 * During this event, {@link CryptoHelper} still uses the previous secret.
 * </p>
 */
@RequiredArgsConstructor
public class MasterSecretRotationEvent {

	/**
	 * Encryptor of the previous master secret.
	 */
	@Getter
	private final StringEncryptor previous;

	/**
	 * Encryptor of the new master secret.
	 */
	@Getter
	private final StringEncryptor next;

	private final AtomicInteger count = new AtomicInteger();

	/**
	 * Decrypt a value with the previous secret, and encrypt it with the new one.
	 *
	 * @param encrypted The value encrypted with the previous secret. May be <code>null</code>.
	 * @return The value encrypted with the new secret. <code>null</code> when the given value is <code>null</code>.
	 * @throws EncryptionOperationNotPossibleException When the value is not encrypted with the previous secret.
	 */
	public String rotate(final String encrypted) {
		if (encrypted == null) {
			return null;
		}
		final var result = next.encrypt(previous.decrypt(encrypted));
		count.incrementAndGet();
		return result;
	}

	/**
	 * Decrypt a potentially encrypted value with the previous secret, and encrypt it with the new one.
	 *
	 * @param value The value, encrypted or not with the previous secret. May be <code>null</code>.
	 * @return The value encrypted with the new secret, or the given value when it is not encrypted.
	 */
	public String rotateAsNeeded(final String value) {
		try {
			return rotate(value);
		} catch (final EncryptionOperationNotPossibleException _) {
			// This value was not encrypted
			return value;
		}
	}

	/**
	 * Return the amount of values encrypted again so far.
	 *
	 * @return The amount of values encrypted again so far.
	 */
	public int getCount() {
		return count.get();
	}
}
