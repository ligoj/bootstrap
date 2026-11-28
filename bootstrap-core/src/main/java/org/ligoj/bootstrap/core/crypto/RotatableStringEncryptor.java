/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import lombok.Getter;
import lombok.Setter;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.PBEConfig;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link StringEncryptor} delegating to a pool of encryptors that can be replaced at runtime by the one of a new
 * master password, see {@link #rotate(StringEncryptor)}. The encryptions and the decryptions of the other threads
 * wait for the end of a rotation.
 */
public class RotatableStringEncryptor implements StringEncryptor, InitializingBean {

	/**
	 * Configuration of the encryptors, and source of the initial master password.
	 */
	@Getter
	@Setter
	private SystemEnvironmentAndFilePBEConfig config;

	/**
	 * Size of the pool of encryptors: a single one serializes all the encryptions and decryptions.
	 */
	@Setter
	private int poolSize = 4;

	/**
	 * Current encryptor.
	 */
	private volatile StringEncryptor delegate;

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	@Override
	public void afterPropertiesSet() {
		// The password is read by the first usage of this encryptor
		delegate = newEncryptor(config);
	}

	private StringEncryptor newEncryptor(final PBEConfig pbeConfig) {
		final var encryptor = new PooledPBEStringEncryptor();
		encryptor.setConfig(pbeConfig);
		encryptor.setPoolSize(poolSize);
		return encryptor;
	}

	/**
	 * Return a new encryptor having the configuration of this one, with another master password.
	 *
	 * @param password The master password of the new encryptor.
	 * @return The new encryptor, not yet used by this one.
	 */
	public StringEncryptor newEncryptor(final String password) {
		final var newConfig = new SimplePBEConfig();
		newConfig.setAlgorithm(config.getAlgorithm());
		newConfig.setKeyObtentionIterations(config.getKeyObtentionIterations());
		newConfig.setSaltGenerator(config.getSaltGenerator());
		newConfig.setIvGenerator(config.getIvGenerator());
		newConfig.setProviderName(config.getProviderName());
		newConfig.setProvider(config.getProvider());
		newConfig.setPassword(password);
		return newEncryptor(newConfig);
	}

	@Override
	public String encrypt(final String message) {
		lock.readLock().lock();
		try {
			return delegate.encrypt(message);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String decrypt(final String encryptedMessage) {
		lock.readLock().lock();
		try {
			return delegate.decrypt(encryptedMessage);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Start a rotation: the encryptions and the decryptions of the other threads wait for {@link #unlock()}. The
	 * current thread still uses the current encryptor.
	 */
	public void lock() {
		lock.writeLock().lock();
	}

	/**
	 * Replace the current encryptor. The current thread must own the rotation, see {@link #lock()}.
	 *
	 * @param encryptor The encryptor replacing the current one, see {@link #newEncryptor(String)}.
	 */
	public void rotate(final StringEncryptor encryptor) {
		if (!lock.isWriteLockedByCurrentThread()) {
			throw new IllegalStateException("The rotation is not owned by the current thread");
		}
		delegate = encryptor;
	}

	/**
	 * End a rotation started by the current thread.
	 */
	public void unlock() {
		lock.writeLock().unlock();
	}
}
