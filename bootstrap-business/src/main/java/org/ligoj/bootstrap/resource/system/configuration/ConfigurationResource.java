/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ligoj.bootstrap.core.AuditedBean;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.dao.system.SystemConfigurationRepository;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.stereotype.Service;

/**
 * Manage {@link SystemConfiguration}. Should be protected with ORBAC rules. Order rules:
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
		return ObjectUtils.defaultIfNull(self.get(key), defaultValue);
	}

	/**
	 * Return a specific configuration. System properties overrides the value from the database. Value is decrypted as
	 * needed.
	 *
	 * @param name The requested configuration name.
	 * @return A specific configuration. May be <code>null</code> when undefined.
	 */
	@CacheResult(cacheName = "configuration")
	@Produces(MediaType.TEXT_PLAIN)
	public String get(@CacheKey @PathParam("name") final String name) {
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
		final Map<String, ConfigurationVo> result = new TreeMap<>();

		// First add the system properties
		env.getPropertySources().forEach(source -> {
			if (source instanceof EnumerablePropertySource) {
				final var eSource = (EnumerablePropertySource<?>) source;
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
				result.get(c.getName()).setOverride(true);
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

	private void updateVo(final String value, final ConfigurationVo vo) {
		if (value.equals(cryptoHelper.decryptAsNeeded(value))) {
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
	public void put(@CacheKey @PathParam("name") final String name, @CacheValue @NotBlank @NotNull final String value) {
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
	public void put(@CacheKey @PathParam("name") final String name, @CacheValue @NotBlank @NotNull final String value,
			@PathParam("system") final boolean system) {
		put(name, value, system, false);
	}

	/**
	 * Save or update a configuration.
	 *
	 * @param name    The configuration name.
	 * @param value   The new value.
	 * @param system  When <code>true</code>, the system variable is also updated.
	 * @param secured When <code>true</code>, the stored value will be secured: never returned to the end user. For
	 *                <code>system</code> variable, a clear value will be used.
	 */
	@POST
	@PUT
	@Path("{name}/{system}/{secured}")
	@CachePut(cacheName = "configuration")
	public void put(@CacheKey @PathParam("name") final String name, @CacheValue @NotBlank @NotNull final String value,
			@PathParam("system") final boolean system, @PathParam("secured") final boolean secured) {
		final var setting = repository.findByName(name);
		final var storedValue = secured ? cryptoHelper.encrypt(value) : value;
		if (setting == null) {
			final var entity = new SystemConfiguration();
			entity.setName(name);
			entity.setValue(storedValue);
			repository.saveAndFlush(entity);
		} else {
			setting.setValue(storedValue);
		}

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
	public void put(final ConfigurationEditionVo conf) {
		self.put(conf.getName(), conf.getValue(), conf.isSystem(), conf.isSecured());
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
