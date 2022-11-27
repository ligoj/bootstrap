/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

import lombok.Setter;

/**
 * Share all non localized properties loaded by Spring.
 */
public class GlobalPropertyUtils extends PropertySourcesPlaceholderConfigurer {

	/**
	 * Global properties.
	 */
	private static final Properties props = new Properties();

	/**
	 * Attached and validated locations.
	 */
	public static Resource[] locations = new Resource[0];

	/*
	 * Shared encryptor.
	 */
	@Setter
	private static StringEncryptor stringEncryptor;

	@Override
	protected void loadProperties(final Properties props) throws IOException {
		super.setLocations(locations);
		setIgnoreUnresolvablePlaceholders(true);

		// Load the properties
		super.loadProperties(props);

		// Save the global properties
		GlobalPropertyUtils.props.putAll(props);
	}

	@Override
	public void setLocations(final Resource... locations) {
		// Cleanup resources to avoid useless WAR
		final var newLocations = new ArrayList<Resource>(locations.length);
		for (final var location : locations) {
			try (var inputStream = location.getInputStream()) {
				if (inputStream != null) {
					newLocations.add(location);
				}
			} catch (final IOException ioe) { // NOSONAR - Check error, no pollution required
				logger.warn(String.format("Ignoring location %s since is not found : %s", location, ioe.getMessage()));
			}
		}
		final Resource[] newLocationsArray;
		if (newLocations.size() == locations.length) {
			newLocationsArray = locations;
		} else {
			newLocationsArray = newLocations.toArray(new Resource[0]);
		}

		// Increase the application properties
		GlobalPropertyUtils.locations =ArrayUtils.addAll(GlobalPropertyUtils.locations, newLocationsArray);

		// Add the locations to the bean
		super.setLocations(GlobalPropertyUtils.locations);
	}

	/**
	 * Return a property loaded by the placeholder.
	 *
	 * @param name the property name.
	 * @return the property value.
	 */
	public static String getProperty(final String name) {
		return props.getProperty(name);
	}

	@Override
	protected String convertPropertyValue(@Nullable final String originalValue) {
		if (PropertyValueEncryptionUtils.isEncryptedValue(originalValue)) {
			return PropertyValueEncryptionUtils.decrypt(originalValue, GlobalPropertyUtils.stringEncryptor);
		}
		return originalValue;
	}

	@Override
	protected void doProcessProperties(ConfigurableListableBeanFactory factory,
	                                   StringValueResolver resolver) {
		super.doProcessProperties(factory, v -> convertPropertyValue(resolver.resolveStringValue(v)));
	}
}
