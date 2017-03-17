package org.ligoj.bootstrap.core.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.springframework.core.io.ClassPathResource;

import org.ligoj.bootstrap.core.GlobalPropertyUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Fail safe password configuration using in this order :
 * <ul>
 * <li>System property</li>
 * <li>Environment variable</li>
 * <li>Password file content (system then environment file name)</li>
 * </ul>
 */
@Slf4j
public class SystemEnvironmentAndFilePBEConfig extends SimplePBEConfig {

	@Setter
	private String passwordEnvName;
	@Setter
	private String passwordSysPropertyName;
	@Setter
	private String passwordFileEnvName;
	@Setter
	private String passwordFilePropertyName;

	/**
	 * Set the configuration object to use the specified file name to load the value for the password. The password is
	 * trimmed to <code>null</code>.
	 * 
	 * @param passwordFilename
	 *            the name of the file name to load.
	 */
	protected String getPasswordFromFile(final String passwordFilename) {

		// Read password from file
		try {
			return StringUtils.trimToNull(FileUtils.readFileToString(new File(passwordFilename), StandardCharsets.UTF_8.name()));
		} catch (final IOException ioe) { // NOSONAR - Safely ignore this fails, and try the next method 
			log.warn("Unable to read file " + passwordFilename);
		}

		// Read password from the classpath
		try {
			return StringUtils.trimToNull(IOUtils.toString(new ClassPathResource(passwordFilename).getInputStream(), StandardCharsets.UTF_8.name()));
		} catch (final IOException ioe) { // NOSONAR - Safely ignore this fails, and assume there is no password
			log.warn("Unable to read resource " + passwordFilename);
		}
		return null;
	}

	/**
	 * Read a password value from the piped readers.
	 * 
	 * @param property
	 *            Initial property to read.
	 * @param pipedReaders
	 *            The ordered readers. Stopped when <code>null</code> is returned.
	 */
	@SafeVarargs
	private final String pipe(final String property, final Function<String, String>... pipedReaders) {
		String value = property;
		for (final Function<String, String> reader : pipedReaders) {
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
		String password = pipe(passwordSysPropertyName, System::getProperty);
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
