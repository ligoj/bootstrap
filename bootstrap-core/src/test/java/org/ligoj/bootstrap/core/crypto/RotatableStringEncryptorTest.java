/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test class of {@link RotatableStringEncryptor} and {@link MasterSecretRotationEvent}.
 */
class RotatableStringEncryptorTest {

	private RotatableStringEncryptor encryptor;

	@BeforeEach
	void setUp() {
		final var config = new SystemEnvironmentAndFilePBEConfig() {
			@Override
			public char[] getPasswordCharArray() {
				return "old".toCharArray();
			}
		};
		config.setAlgorithm("PBEWithMD5AndDES");
		encryptor = new RotatableStringEncryptor();
		encryptor.setConfig(config);
		encryptor.setPoolSize(2);
		encryptor.afterPropertiesSet();
	}

	@Test
	void encryptDecrypt() {
		final var encrypted = encryptor.encrypt("value");
		Assertions.assertNotEquals("value", encrypted);
		Assertions.assertEquals("value", encryptor.decrypt(encrypted));
		Assertions.assertEquals("PBEWithMD5AndDES", encryptor.getConfig().getAlgorithm());
	}

	@Test
	void newEncryptor() {
		final var next = encryptor.newEncryptor("new");
		final var encrypted = encryptor.encrypt("value");
		Assertions.assertThrows(EncryptionOperationNotPossibleException.class, () -> next.decrypt(encrypted));
		Assertions.assertEquals("value", next.decrypt(next.encrypt("value")));

		// Same configuration
		Assertions.assertEquals("value", encryptor.newEncryptor("old").decrypt(encrypted));
	}

	@Test
	void rotateNotLocked() {
		final var next = encryptor.newEncryptor("new");
		Assertions.assertThrows(IllegalStateException.class, () -> encryptor.rotate(next));
	}

	@Test
	void rotate() throws ExecutionException, InterruptedException, TimeoutException {
		final var next = encryptor.newEncryptor("new");
		final var encrypted = encryptor.encrypt("value");
		encryptor.lock();
		final CompletableFuture<String> other;
		try {
			// The other threads wait, the current one uses the previous secret
			other = CompletableFuture.supplyAsync(() -> encryptor.decrypt(next.encrypt("other")));
			Assertions.assertThrows(TimeoutException.class, () -> other.get(200, TimeUnit.MILLISECONDS));
			Assertions.assertEquals("value", encryptor.decrypt(encrypted));
			encryptor.rotate(next);
		} finally {
			encryptor.unlock();
		}
		Assertions.assertEquals("other", other.get(10, TimeUnit.SECONDS));
		Assertions.assertEquals("value", encryptor.decrypt(next.encrypt("value")));
		Assertions.assertThrows(EncryptionOperationNotPossibleException.class, () -> encryptor.decrypt(encrypted));
	}

	@Test
	void event() {
		final var next = encryptor.newEncryptor("new");
		final var event = new MasterSecretRotationEvent(encryptor, next);
		Assertions.assertSame(encryptor, event.getPrevious());
		Assertions.assertSame(next, event.getNext());
		Assertions.assertNull(event.rotate(null));
		Assertions.assertNull(event.rotateAsNeeded(null));
		Assertions.assertEquals("plain", event.rotateAsNeeded("plain"));
		Assertions.assertEquals(0, event.getCount());
		Assertions.assertThrows(EncryptionOperationNotPossibleException.class, () -> event.rotate("plain"));

		Assertions.assertEquals("value", next.decrypt(event.rotate(encryptor.encrypt("value"))));
		Assertions.assertEquals("value", next.decrypt(event.rotateAsNeeded(encryptor.encrypt("value"))));
		Assertions.assertEquals(2, event.getCount());
	}
}
