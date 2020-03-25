/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.StringValueResolver;

import lombok.Setter;

/**
 * Share all non localized properties loaded by Spring.
 */
public class GlobalPropertyUtils extends PropertySourcesPlaceholderConfigurer {

	/**
	 * Global properties.
	 */
	private static Properties props = null;

	/**
	 * Attached and validated locations.
	 */
	private static Resource[] locations = new Resource[0];

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
		setGlobalProperties(props);
	}

	@Override
	public void setLocations(final Resource... locations) {
		// Invalidate previous cache
		if (GlobalPropertyUtils.props != null) {
			setGlobalProperties(null);
			setGlobalLocations();
		}

		// Cleanup resources to avoid useless WAR
		final List<Resource> newLocations = new ArrayList<>(locations.length);
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

		// Add the locations to the bean
		super.setLocations(locations);

		// Increase the application properties
		setGlobalLocations(ArrayUtils.addAll(GlobalPropertyUtils.locations, newLocationsArray));
	}

	/**
	 * Set the global locations.
	 *
	 * @param locations The global resources.
	 */
	public static void setGlobalLocations(final Resource... locations) {
		GlobalPropertyUtils.locations = locations;
	}

	/**
	 * Set the global properties.
	 *
	 * @param props The global properties.
	 */
	public static void setGlobalProperties(final Properties props) {
		GlobalPropertyUtils.props = props;
	}

	/**
	 * Return a property loaded by the place holder.
	 *
	 * @param name the property name.
	 * @return the property value.
	 */
	public static String getProperty(final String name) {
		return props.getProperty(name);
	}

	@Override
	protected String convertPropertyValue(final String originalValue) {
		if (!PropertyValueEncryptionUtils.isEncryptedValue(originalValue)) {
			return originalValue;
		}
		return PropertyValueEncryptionUtils.decrypt(originalValue, GlobalPropertyUtils.stringEncryptor);
	}

	@Override
	protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			StringValueResolver valueResolver) {
		super.doProcessProperties(beanFactoryToProcess,
				strVal -> convertPropertyValue(valueResolver.resolveStringValue(strVal)));
	}
}
