/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.ligoj.bootstrap.core.GlobalPropertyUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

/**
 * Fail-safe password configuration. When the external secret manager is configured, see {@link VaultPasswordReader},
 * it is the only source: the password is read once from it, and a failure is not recovered by another source.
 * Otherwise, the first defined value in this order is used:
 * <ul>
 * <li>System property</li>
 * <li>Loaded properties, see {@link GlobalPropertyUtils}</li>
 * <li>Environment variable</li>
 * <li>Password file content, the file name being read from the system property, then the loaded properties, then the
 * environment variable. The file is looked up in the file system, then in the classpath.</li>
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
	 * Optional reader of the password from an external secret manager.
	 */
	private VaultPasswordReader vaultPasswordReader = new VaultPasswordReader();

	/**
	 * When <code>true</code>, the password has been read from the external secret manager: it is called only once.
	 */
	@Setter(AccessLevel.NONE)
	private boolean vaultRead;

	/**
	 * Load the password from the specified file, then from the classpath resource having this name. The password is
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

	/**
	 * Read once the password from the external secret manager, the other sources being ignored. The password is
	 * only kept by the parent class, until it is cleaned by the encryptor.
	 */
	private void readVaultPassword() {
		if (!vaultRead) {
			final var ignored = new TreeSet<String>();
			addDefined(ignored, passwordSysPropertyName, System::getProperty);
			addDefined(ignored, passwordSysPropertyName, GlobalPropertyUtils::getProperty);
			addDefined(ignored, passwordEnvName, System::getenv);
			addDefined(ignored, passwordFilePropertyName, System::getProperty);
			addDefined(ignored, passwordFilePropertyName, GlobalPropertyUtils::getProperty);
			addDefined(ignored, passwordFileEnvName, System::getenv);
			if (!ignored.isEmpty()) {
				log.warn("OpenBao is the only source of the master password, ignored settings: {}", ignored);
			}
			setPassword(vaultPasswordReader.read());
			vaultRead = true;
		}
	}

	/**
	 * Add the setting name to the given ones when it is defined for the given reader.
	 */
	private void addDefined(final Set<String> names, final String name, final UnaryOperator<String> reader) {
		if (pipe(name, reader) != null) {
			names.add(name);
		}
	}

	@Override
	public char[] getPasswordCharArray() {
		if (vaultPasswordReader.isEnabled()) {
			// Exclusive source
			readVaultPassword();
			return super.getPasswordCharArray();
		}

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
