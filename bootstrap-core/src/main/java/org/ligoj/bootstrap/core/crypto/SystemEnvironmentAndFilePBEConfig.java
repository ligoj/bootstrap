/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.UnaryOperator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.ligoj.bootstrap.core.GlobalPropertyUtils;
import org.springframework.core.io.ClassPathResource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Fail-safe password configuration using in this order :
 * <ul>
 * <li>System property</li>
 * <li>Environment variable</li>
 * <li>Password file content (system then environment file name)</li>
 * </ul>
 */
@Slf4j
@Setter
public class SystemEnvironmentAndFilePBEConfig extends SimplePBEConfig {

	private String passwordEnvName;
	private String passwordSysPropertyName;
	private String passwordFileEnvName;
	private String passwordFilePropertyName;

	/**
	 * Set the configuration object to use the specified file name to load the value for the password. The password is
	 * trimmed to <code>null</code>.
	 *
	 * @param passwordFilename the name of the file name to load.
	 * @return The resolved password from given file or <code>null</code> if failed.
	 */
	protected String getPasswordFromFile(final String passwordFilename) {

		// Read password from file
		try {
			return StringUtils
					.trimToNull(FileUtils.readFileToString(new File(passwordFilename), StandardCharsets.UTF_8));
		} catch (final IOException ioe) { // NOSONAR - Safely ignore this fails, and try the next method
			log.warn("Unable to read file {} : {}", passwordFilename, ioe.getMessage());
		}

		// Read password from the classpath
		try {
			return StringUtils.trimToNull(IOUtils.toString(new ClassPathResource(passwordFilename).getInputStream(),
					StandardCharsets.UTF_8));
		} catch (final IOException ioe) { // NOSONAR - Safely ignore this fails, and assume there is no password
			log.warn("Unable to read resource {} : {}", passwordFilename, ioe.getMessage());
		}
		return null;
	}

	/**
	 * Read a password value from the piped readers.
	 *
	 * @param property     Initial property to read.
	 * @param pipedReaders The ordered readers. Stopped when <code>null</code> is returned.
	 */
	@SafeVarargs
	private String pipe(final String property, final UnaryOperator<String>... pipedReaders) {
		var value = property;
		for (final var reader : pipedReaders) {
			if (StringUtils.isNotBlank(value)) {
				value = reader.apply(value);
			}
		}
		return value;
	}

	@Override
	public String getPassword() {
		return new String(getPasswordCharArray());
	}

	@Override
	public char[] getPasswordCharArray() {
		// Raw value providers
		var password = pipe(passwordSysPropertyName, System::getProperty);
		if (password == null) {
			password = pipe(passwordSysPropertyName, GlobalPropertyUtils::getProperty);
		}
		if (password == null) {
			password = pipe(passwordEnvName, System::getenv);
		}

		// File providers
		if (password == null) {
			password = pipe(passwordFilePropertyName, System::getProperty, this::getPasswordFromFile);
		}
		if (password == null) {
			password = pipe(passwordFilePropertyName, GlobalPropertyUtils::getProperty, this::getPasswordFromFile);
		}
		if (password == null) {
			password = pipe(passwordFileEnvName, System::getenv, this::getPasswordFromFile);
		}
		setPassword(password);
		return super.getPasswordCharArray();
	}
}
