/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.crypto.RotatableStringEncryptor;
import org.ligoj.bootstrap.core.crypto.SystemEnvironmentAndFilePBEConfig;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.ligoj.bootstrap.model.system.SystemMfaDevice;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Test class of the master secret rotation of {@link ConfigurationResource}
 */
@ExtendWith(SpringExtension.class)
class ConfigurationResourceRotateTest extends AbstractBootTest {

	@Autowired
	private ConfigurationResource resource;

	@Autowired
	private RotatableStringEncryptor encryptor;

	private StringEncryptor originalDelegate;
	private SystemEnvironmentAndFilePBEConfig originalConfig;
	private SystemEnvironmentAndFilePBEConfig config;
	private SystemConfiguration secured;
	private SystemConfiguration plain;
	private SystemMfaDevice device;
	private String securedValue;

	@BeforeEach
	void prepare() {
		// Never update the real source of the master secret
		originalDelegate = (StringEncryptor) ReflectionTestUtils.getField(encryptor, "delegate");
		originalConfig = encryptor.getConfig();
		config = Mockito.spy(originalConfig);
		Mockito.doReturn(null).when(config).store(Mockito.anyString());
		encryptor.setConfig(config);

		securedValue = cryptoHelper.encrypt("value-secured");
		secured = new SystemConfiguration();
		secured.setName("test-rotate-secured");
		secured.setValue(securedValue);
		em.persist(secured);
		plain = new SystemConfiguration();
		plain.setName("test-rotate-plain");
		plain.setValue("value-plain");
		em.persist(plain);
		device = new SystemMfaDevice();
		device.setUser(DEFAULT_USER);
		device.setName("test-rotate");
		device.setSecret(cryptoHelper.encrypt("device-secret"));
		em.persist(device);
		em.flush();
		initSpringSecurityContext(DEFAULT_USER, new SimpleGrantedAuthority(SecurityHelper.ADMIN));
	}

	@AfterEach
	void restore() {
		// End the pending rotation while the real source is still protected, then restore the shared encryptor
		complete(TransactionSynchronization.STATUS_ROLLED_BACK);
		encryptor.setConfig(originalConfig);
		encryptor.lock();
		try {
			encryptor.rotate(originalDelegate);
		} finally {
			encryptor.unlock();
		}
	}

	/**
	 * Complete the rotation as its transaction would do.
	 */
	private void complete(final int status) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.getSynchronizations().stream()
					.filter(s -> s.getClass().getName().contains("RotationCompletion")).forEach(s -> s.afterCompletion(status));
		}
	}

	private void refresh() {
		em.flush();
		em.clear();
		secured = em.find(SystemConfiguration.class, secured.getId());
		plain = em.find(SystemConfiguration.class, plain.getId());
		device = em.find(SystemMfaDevice.class, device.getId());
	}

	@Test
	void rotateNotAdmin() {
		initSpringSecurityContext(DEFAULT_USER);
		Assertions.assertThrows(AccessDeniedException.class, resource::rotate);
		Mockito.verify(config, Mockito.never()).store(Mockito.anyString());
	}

	@Test
	void rotateNotAdminWithoutMethodSecurity() {
		initSpringSecurityContext(DEFAULT_USER);
		final ConfigurationResource target = AopTestUtils.getUltimateTargetObject(resource);
		Assertions.assertThrows(AccessDeniedException.class, target::rotate);
		Mockito.verify(config, Mockito.never()).store(Mockito.anyString());
	}

	@Test
	void rotateReturnedSecret() {
		final var result = resource.rotate();
		Assertions.assertEquals(2, result.getCount());
		Assertions.assertEquals(43, result.getSecret().length());
		Assertions.assertNotNull(result.getProperties());
		Mockito.verify(config).store(result.getSecret());

		// Values are updated, the previous secret is still used
		refresh();
		Assertions.assertNotEquals(securedValue, secured.getValue());
		Assertions.assertEquals("value-plain", plain.getValue());
		Assertions.assertEquals("value-secured", cryptoHelper.decrypt(securedValue));
		final var newSecured = secured.getValue();
		Assertions.assertThrows(EncryptionOperationNotPossibleException.class, () -> cryptoHelper.decrypt(newSecured));

		// The new secret is used
		complete(TransactionSynchronization.STATUS_COMMITTED);
		Assertions.assertEquals("value-secured", cryptoHelper.decrypt(secured.getValue()));
		Assertions.assertEquals("device-secret", cryptoHelper.decrypt(device.getSecret()));
		Assertions.assertEquals("value-secured", resource.get("test-rotate-secured"));
		Assertions.assertEquals("value-plain", resource.get("test-rotate-plain"));
		Assertions.assertThrows(EncryptionOperationNotPossibleException.class, () -> cryptoHelper.decrypt(securedValue));

		// The returned secret is the new one
		Assertions.assertEquals("value-secured", encryptor.newEncryptor(result.getSecret()).decrypt(secured.getValue()));

		// The rotation is completed only once
		complete(TransactionSynchronization.STATUS_ROLLED_BACK);
		Mockito.verify(config, Mockito.times(1)).store(Mockito.anyString());
	}

	@Test
	void rotateSavedSecret() {
		Mockito.doReturn("previous").when(config).store(Mockito.anyString());
		final var result = resource.rotate();
		Assertions.assertEquals(2, result.getCount());
		Assertions.assertNull(result.getSecret());
		complete(TransactionSynchronization.STATUS_COMMITTED);
		refresh();
		Assertions.assertEquals("value-secured", cryptoHelper.decrypt(secured.getValue()));
		Mockito.verify(config, Mockito.times(1)).store(Mockito.anyString());
	}

	@Test
	void rotateTwice() {
		resource.rotate();
		complete(TransactionSynchronization.STATUS_COMMITTED);
		final var result = resource.rotate();
		Assertions.assertEquals(2, result.getCount());
		complete(TransactionSynchronization.STATUS_COMMITTED);
		refresh();
		Assertions.assertEquals("value-secured", cryptoHelper.decrypt(secured.getValue()));
		Assertions.assertEquals("device-secret", cryptoHelper.decrypt(device.getSecret()));
	}

	@Test
	void rotateRollbackReturnedSecret() {
		resource.rotate();
		complete(TransactionSynchronization.STATUS_ROLLED_BACK);

		// The previous secret is still used
		Assertions.assertEquals("value-secured", cryptoHelper.decrypt(securedValue));
		Mockito.verify(config, Mockito.times(1)).store(Mockito.anyString());
	}

	@Test
	void rotateRollbackSavedSecret() {
		Mockito.doReturn("previous").when(config).store(Mockito.anyString());
		resource.rotate();
		complete(TransactionSynchronization.STATUS_ROLLED_BACK);

		// The previous secret is still used, and is restored in its source
		Assertions.assertEquals("value-secured", cryptoHelper.decrypt(securedValue));
		Mockito.verify(config).store("previous");
		Mockito.verify(config, Mockito.times(2)).store(Mockito.anyString());
	}

	@Test
	void rotateRollbackNotRestored() throws ExecutionException, InterruptedException, TimeoutException {
		Mockito.doReturn("previous").when(config).store(Mockito.anyString());
		resource.rotate();
		Mockito.doThrow(new EncryptionInitializationException("down")).when(config).store("previous");
		complete(TransactionSynchronization.STATUS_ROLLED_BACK);

		// The other threads are no more waiting
		Assertions.assertEquals("value-secured",
				CompletableFuture.supplyAsync(() -> cryptoHelper.decrypt(securedValue)).get(10, TimeUnit.SECONDS));
	}

	@Test
	void rotateWithoutTransaction() throws ExecutionException, InterruptedException, TimeoutException {
		TestTransaction.end();
		final ConfigurationResource target = AopTestUtils.getUltimateTargetObject(resource);
		Assertions.assertThrows(IllegalStateException.class, target::rotate);
		Mockito.verify(config, Mockito.never()).store(Mockito.anyString());

		// The other threads are not waiting
		Assertions.assertEquals("value", CompletableFuture.supplyAsync(() -> cryptoHelper.decrypt(cryptoHelper.encrypt("value")))
				.get(10, TimeUnit.SECONDS));
	}
}
