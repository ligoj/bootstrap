/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.ligoj.bootstrap.core.AuditedBean;
import org.jasypt.encryption.StringEncryptor;
import org.ligoj.bootstrap.core.GlobalPropertyUtils;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.core.crypto.MasterSecretRotationEvent;
import org.ligoj.bootstrap.core.crypto.RotatableStringEncryptor;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.dao.system.SystemConfigurationRepository;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import java.security.SecureRandom;
import java.util.*;

/**
 * Manage {@link SystemConfiguration}. Should be protected with O-RBAC rules. Order rules:
 * <ul>
 * <li>System properties</li>
 * <li>Environment properties</li>
 * <li>Database properties</li>
 * </ul>
 */
@Service
@Path("/system/configuration")
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class ConfigurationResource {

	/**
	 * Size in bytes of a generated master secret.
	 */
	private static final int SECRET_SIZE = 32;

	@Autowired
	private RotatableStringEncryptor encryptor;

	@Autowired
	private SecurityHelper securityHelper;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private CryptoHelper cryptoHelper;

	@Autowired
	private SystemConfigurationRepository repository;

	@Autowired
	private ConfigurationResource self;

	@Autowired
	protected ConfigurableEnvironment env;

	/**
	 * Return the configuration integer value.
	 *
	 * @param key          The configuration key name.
	 * @param defaultValue The default integer value when <code>null</code>
	 * @return the configuration integer value or the default value.
	 */
	public int get(final String key, final int defaultValue) {
		return NumberUtils.toInt(self.get(key), defaultValue);
	}

	/**
	 * Return the configuration value.
	 *
	 * @param key          The configuration key name.
	 * @param defaultValue The default value when <code>null</code>
	 * @return the configuration value or the default value.
	 */
	public String get(final String key, final String defaultValue) {
		return ObjectUtils.getIfNull(self.get(key), defaultValue);
	}

	/**
	 * Return a specific configuration. System properties overrides the value from the database. Value is decrypted as
	 * needed.
	 *
	 * @param name The requested configuration name.
	 * @return A specific configuration. May be <code>null</code> when undefined.
	 */
	@CacheResult(cacheName = "configuration")
	public String get(@CacheKey final String name) {
		return Optional.ofNullable(getRaw(name)).map(cryptoHelper::decryptAsNeeded).orElse(null);
	}

	/**
	 * Return a specific configuration. System properties overrides the value from the database. Encrypted
	 * values are never returned: <code>null</code> is returned instead.
	 *
	 * @param name The requested configuration name.
	 * @return A specific configuration. May be <code>null</code> when undefined or encrypted.
	 */
	@GET
	@Path("{name}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUnSecuredOnly(@CacheKey @PathParam("name") final String name) {
		return cryptoHelper.decryptedOnly(getRaw(name));
	}

	/**
	 * Return the configuration value with decryption.
	 * - [spring] Command-line arguments (spring application)
	 * - [spring] Java System properties
	 * - [spring] OS environment variables
	 * - [spring] Application properties or YAML files
	 * - [spring] Profile-specific configuration files
	 * - [spring] External configuration files
	 * - [spring] JNDI attributes
	 * - [ligoj] Database
	 */
	private String getRaw(final String name) {
		var value = StringUtils.trimToNull(env.getProperty(name));
		if (value == null) {
			value = Optional.ofNullable(repository.findByName(name)).map(SystemConfiguration::getValue)
					.map(StringUtils::trimToNull).orElse(null);
		}
		return value;
	}

	/**
	 * Return a merged list of properties from the system properties and entity {@link SystemConfiguration}.
	 *
	 * @return All defined configurations, either from {@link System} either from {@link SystemConfiguration}.
	 */
	@GET
	public List<ConfigurationVo> findAll() {
		final var result = new TreeMap<String, ConfigurationVo>();

		// First add the system properties
		env.getPropertySources().forEach(source -> {
			if (source instanceof EnumerablePropertySource<?> eSource) {
				Arrays.stream(eSource.getPropertyNames()).map(v -> {
					final var vo = new ConfigurationVo();
					vo.setName(v);
					vo.setSource(eSource.getName());
					updateVo(String.valueOf(eSource.getProperty(v)), vo);
					return vo;
				}).forEach(vo -> result.put(vo.getName(), vo));
			}
		});

		// Add the JPA not yet managed
		repository.findAll().forEach(c -> {
			if (result.containsKey(c.getName())) {
				final var vo = result.get(c.getName());
				// The audit data of the stored value, even when a property source shadows it
				AuditedBean.copyAuditData(c, vo);
				vo.setPersisted(true);
				vo.setOverridden(!Strings.CS.equals(vo.getValue(), c.getValue()));
			} else {
				final var vo = new ConfigurationVo();
				AuditedBean.copyAuditData(c, vo);
				vo.setPersisted(true);
				vo.setName(c.getName());
				vo.setSource("database");
				updateVo(c.getValue(), vo);
				result.put(c.getName(), vo);
			}
		});

		// Return sorted values by their name
		return new ArrayList<>(result.values());
	}

	/**
	 * Set a value is not an encrypted one.
	 *
	 * @param value The raw, maybe encrypted, value
	 * @param vo    The target value wrapper.
	 */
	protected void updateVo(final String value, final ConfigurationVo vo) {
		if (value == null || value.equals(cryptoHelper.decryptAsNeeded(value))) {
			vo.setValue(value);
		} else {
			// Do not expose secured value, even hashed data
			vo.setSecured(true);
		}
	}

	/**
	 * Rotate the master secret: a new secret is generated, all the encrypted values of the database are encrypted
	 * again with it in this transaction, the new secret is saved in its source when this is possible, then it replaces
	 * the current one in this application, without restart. Reserved to administrators.
	 * <ul>
	 * <li>The configuration values are updated by this resource, the other values by the listeners of the
	 * {@link MasterSecretRotationEvent} published in this transaction, just before the new secret is used. A failing
	 * listener cancels the rotation.</li>
	 * <li>The new secret is saved in the external secret manager when it is configured, otherwise in the file the
	 * current one is read from. When neither is possible, it is only returned.</li>
	 * <li>When the transaction is not committed, the previous secret is restored in its source.</li>
	 * <li>The encryptions and the decryptions of the other requests wait for the end of the rotation.</li>
	 * <li>The other running nodes sharing this database keep the previous secret until their restart.</li>
	 * <li>The <code>ENC(...)</code> values of the properties files are not updated.</li>
	 * </ul>
	 *
	 * @return The amount of updated values, the new secret only when it has not been saved, and the properties to
	 * update.
	 */
	@POST
	@Path("crypto/rotate")
	@Secured(SecurityHelper.ADMIN)
	public RotationVo rotate() {
		// Also checked here: the method security may not be enabled by the consumer
		if (!securityHelper.isAdmin()) {
			throw new AccessDeniedException("Administrator only");
		}
		final var bytes = new byte[SECRET_SIZE];
		new SecureRandom().nextBytes(bytes);
		final var secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		final var event = new MasterSecretRotationEvent(encryptor, encryptor.newEncryptor(secret));
		final var completion = new RotationCompletion(encryptor, event.getNext());

		// The other threads wait for the end of the transaction
		encryptor.lock();
		try {
			TransactionSynchronizationManager.registerSynchronization(completion);
		} catch (final RuntimeException e) {
			encryptor.unlock();
			throw e;
		}

		// Encrypt again the values of this resource, then the ones of the listeners
		repository.findAll().forEach(c -> c.setValue(event.rotateAsNeeded(c.getValue())));
		publisher.publishEvent(event);
		repository.flush();

		// Save the new secret, as late as possible
		completion.previous = encryptor.getConfig().store(secret);
		final var result = new RotationVo();
		result.setCount(event.getCount());
		result.setProperties(GlobalPropertyUtils.getEncryptedProperties());
		if (completion.previous == null) {
			// The caller is the only owner of the new secret
			result.setSecret(secret);
		}
		log.info("Master secret rotated: {} values, {}", result.getCount(),
				completion.previous == null ? "the new secret is not saved" : "the new secret is saved");
		return result;
	}

	/**
	 * End of a rotation with its transaction: the new secret is used, or the previous one is restored.
	 */
	@RequiredArgsConstructor
	private static final class RotationCompletion implements TransactionSynchronization {
		private final RotatableStringEncryptor encryptor;
		private final StringEncryptor next;

		/**
		 * The previous secret when the new one has been saved in its source.
		 */
		private String previous;
		private boolean completed;

		@Override
		public void afterCompletion(final int status) {
			if (completed) {
				return;
			}
			completed = true;
			try {
				if (status == STATUS_COMMITTED) {
					encryptor.rotate(next);
				} else if (previous != null) {
					log.warn("Master secret rotation cancelled, the previous secret is restored in its source");
					restore();
				}
			} finally {
				encryptor.unlock();
			}
		}

		private void restore() {
			try {
				encryptor.getConfig().store(previous);
			} catch (final RuntimeException e) {
				log.error("The previous master secret is not restored in its source, and must be before the next start: {}",
						e.getMessage());
			}
		}
	}

	/**
	 * Save or update a configuration. The system variable is not updated. The stored value will not be secured in
	 * database.
	 *
	 * @param name  The configuration name.
	 * @param value The new value.
	 */
	@POST
	@PUT
	@Path("{name}")
	@CacheRemove(cacheName = "configuration")
	public void put(@CacheKey @PathParam("name") final String name, @NotBlank final String value) {
		put(name, value, false);
	}

	/**
	 * Save or update a setting. The stored value will not be secured in database.
	 *
	 * @param name   The configuration name.
	 * @param value  The new value.
	 * @param system When <code>true</code>, the system variable is also updated.
	 */
	@POST
	@PUT
	@Path("{name}/{system}")
	@CacheRemove(cacheName = "configuration")
	public void put(@CacheKey @PathParam("name") final String name, @NotBlank final String value,
			@PathParam("system") final boolean system) {
		put(name, value, system, false);
	}

	/**
	 * Save or update a configuration.
	 *
	 * @param name    The configuration name.
	 * @param value   The new value. When <code>null</code>, the configuration is deleted.
	 * @param system  When <code>true</code>, the system variable is also updated.
	 * @param secured When <code>true</code>, the stored value will be secured: never returned to the end user. For
	 *                <code>system</code> variable, a clear value will be used.
	 */
	@POST
	@PUT
	@Path("{name}/{system}/{secured}")
	@CacheRemove(cacheName = "configuration")
	public void put(@CacheKey @PathParam("name") final String name, @NotBlank final String value,
			@PathParam("system") final boolean system, @PathParam("secured") final boolean secured) {
		if (value == null) {
			self.delete(name, system);
			return;
		}
		var setting = repository.findByName(name);
		if (setting == null) {
			setting = new SystemConfiguration();
			setting.setName(name);
		}
		final var storedValue = secured ? cryptoHelper.encrypt(value) : value;
		setting.setValue(storedValue);
		repository.saveAndFlush(setting);

		if (system) {
			// Also set the value in the system, not hashed form
			System.setProperty(name, value);
		}
	}

	/**
	 * Save or update a configuration.
	 *
	 * @param conf The new configuration.
	 */
	@POST
	@PUT
	@CacheRemoveAll(cacheName = "configuration")
	public void put(final ConfigurationEditionVo conf) {
		self.put(conf.getName(), conf.getValue(), conf.isSystem(), conf.isSecured());
		if (StringUtils.isNotBlank(conf.getOldName()) && !conf.getOldName().equals(conf.getName())) {
			// This is a renaming, delete the previous name
			self.put(conf.getOldName(), null, conf.isSystem(), conf.isSecured());
		}
	}

	/**
	 * Delete a {@link SystemConfiguration}. The related system property is not cleared.
	 *
	 * @param name The configuration name to delete.
	 */
	@DELETE
	@Path("{name}")
	@CacheRemove(cacheName = "configuration")
	public void delete(@CacheKey @PathParam("name") final String name) {
		delete(name, false);
	}

	/**
	 * Delete a {@link SystemConfiguration}
	 *
	 * @param name   The configuration name to delete.
	 * @param system When <code>true</code>, the system variable is also deleted.
	 */
	@DELETE
	@Path("{name}/{system}")
	@CacheRemove(cacheName = "configuration")
	public void delete(@CacheKey @PathParam("name") final String name, @PathParam("system") final boolean system) {
		repository.deleteAllBy("name", name);
		if (system) {
			System.clearProperty(name);
		}
	}

}
