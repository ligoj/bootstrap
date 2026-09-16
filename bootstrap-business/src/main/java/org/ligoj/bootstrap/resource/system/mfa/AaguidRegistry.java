/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.mfa;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Authenticator models by AAGUID, from the bundled <code>META-INF/mfa-aaguid.properties</code> (lowercase UUID =
 * model name). The AAGUID is disclosed by most security keys and passkey providers even with the "none"
 * attestation; an unknown or zero AAGUID gives no model.
 */
@Slf4j
public final class AaguidRegistry {

	/**
	 * Bundled models resource.
	 */
	public static final String RESOURCE = "META-INF/mfa-aaguid.properties";

	private static final Map<String, String> MODELS = load();

	private AaguidRegistry() {
		// Utility class
	}

	private static Map<String, String> load() {
		final var properties = new Properties();
		try (var in = AaguidRegistry.class.getClassLoader().getResourceAsStream(RESOURCE)) {
			if (in != null) {
				properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
			}
		} catch (final IOException ioe) {
			log.warn("Unable to read the authenticator models from {}", RESOURCE, ioe);
		}
		return properties.stringPropertyNames().stream()
				.collect(Collectors.toMap(k -> k.toLowerCase(), k -> properties.getProperty(k).trim()));
	}

	/**
	 * The model of an authenticator.
	 *
	 * @param aaguid The AAGUID in UUID form, may be <code>null</code>.
	 * @return The model name, <code>null</code> when unknown.
	 */
	public static String model(final String aaguid) {
		return aaguid == null ? null : MODELS.get(aaguid.toLowerCase());
	}

	/**
	 * Number of known models.
	 *
	 * @return The registry size.
	 */
	public static int size() {
		return MODELS.size();
	}
}
