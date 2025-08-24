/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.ligoj.bootstrap.core.AuditedBean;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.dao.system.SystemConfigurationRepository;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.stereotype.Service;

import javax.cache.annotation.*;
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
public class ConfigurationResource {

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
	 * Return the configuration integer value.
	 *
	 * @param key          The configuration key name.
	 * @param defaultValue The default integer value when <code>null</code>
	 * @return the configuration integer value or the default value.
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
	 * Return a specific configuration. System properties overrides the value from the database. Configuration values
	 * are never returned.
	 *
	 * @param name The requested configuration name.
	 * @return A specific configuration. May be <code>null</code> when undefined.
	 */
	@GET
	@Path("{name}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUnSecuredOnly(@CacheKey @PathParam("name") final String name) {
		return cryptoHelper.decryptedOnly(getRaw(name));
	}

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
	 * Save or update a configuration. The system variable is not updated. The stored value will not be secured in
	 * database.
	 *
	 * @param name  The configuration name.
	 * @param value The new value.
	 */
	@POST
	@PUT
	@Path("{name}")
	@CachePut(cacheName = "configuration")
	public void put(@CacheKey @PathParam("name") final String name, @CacheValue @NotBlank final String value) {
		put(name, value, false);
	}

	/**
	 * Save or update a setting and return the corresponding identifier. The stored value will not be secured in
	 * database.
	 *
	 * @param name   The configuration name.
	 * @param value  The new value.
	 * @param system When <code>true</code>, the system variable is also updated.
	 */
	@POST
	@PUT
	@Path("{name}/{system}")
	@CachePut(cacheName = "configuration")
	public void put(@CacheKey @PathParam("name") final String name, @CacheValue @NotBlank final String value,
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
	 * Delete a {@link SystemConfiguration} and also delete the related system property. The system variable is not
	 * updated.
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
