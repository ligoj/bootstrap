package org.ligoj.bootstrap.resource.system.configuration;

import java.util.Optional;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.dao.system.SystemConfigurationRepository;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manage {@link SystemConfiguration}.
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

	/**
	 * Return the configuration integer value.
	 * 
	 * @param key
	 *            The configuration key name.
	 * @param defaultValue
	 *            The default integer value when <code>null</code>
	 * @return the configuration integer value or the default value.
	 */
	public int get(final String key, final int defaultValue) {
		return NumberUtils.toInt(self.get(key), defaultValue);
	}

	/**
	 * Return the configuration integer value.
	 * 
	 * @param key
	 *            The configuration key name.
	 * @param defaultValue
	 *            The default integer value when <code>null</code>
	 * @return the configuration integer value or the default value.
	 */
	public String get(final String key, final String defaultValue) {
		return ObjectUtils.defaultIfNull(self.get(key), defaultValue);
	}

	/**
	 * Return a specific configuration. System properties overrides the value from the database. Configuration values
	 * are
	 * always encrypted.
	 * 
	 * @param name
	 *            The requested parameter name.
	 * @return a specific configuration. May be <code>null</code>.
	 */
	@GET
	@CacheResult(cacheName = "configuration")
	public String get(@CacheKey final String name) {
		String value = System.getProperty(name);
		if (value == null) {
			value = Optional.ofNullable(repository.findByName(name)).map(SystemConfiguration::getValue).orElse(null);
		}
		return Optional.ofNullable(value).map(cryptoHelper::decryptAsNeeded).orElse(null);
	}

	/**
	 * Save or update a setting and return the corresponding identifier.
	 * 
	 * @param name
	 *            the configuration name.
	 * @param value
	 *            the new value.
	 */
	@POST
	@PUT
	@Path("{name}/{value}")
	@CachePut(cacheName = "configuration")
	public void saveOrUpdate(@CacheKey @PathParam("name") final String name, @CacheValue @PathParam("value") final String value) {
		final SystemConfiguration setting = repository.findByName(name);
		if (setting == null) {
			final SystemConfiguration entity = new SystemConfiguration();
			entity.setName(name);
			entity.setValue(cryptoHelper.encrypt(value));
			repository.saveAndFlush(entity);
		} else {
			setting.setValue(cryptoHelper.encrypt(value));
		}
	}

	/**
	 * Delete a {@link SystemConfiguration}
	 * 
	 * @param name
	 *            The configuration name to delete.
	 */
	@DELETE
	@Path("{name}")
	@CacheRemove(cacheName = "configuration")
	public void delete(@CacheKey @PathParam("name") final String name) {
		repository.deleteAllBy("name", name);
	}

}
