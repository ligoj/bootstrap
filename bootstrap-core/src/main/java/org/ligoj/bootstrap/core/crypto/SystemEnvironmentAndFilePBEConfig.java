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
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Fail-safe password configuration. When the external secret manager is configured, see {@link VaultPasswordStore},
 * it is the only source: the password is read once from it, at the latest when the application context is started
 * whereas the encryptor is initialized by its first use, and a failure is not recovered by another source.
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
public class SystemEnvironmentAndFilePBEConfig extends SimplePBEConfig implements SmartInitializingSingleton {

	private String passwordEnvName;
	private String passwordSysPropertyName;
	private String passwordFileEnvName;
	private String passwordFilePropertyName;

	/**
	 * Optional reader of the password from an external secret manager.
	 */
	private VaultPasswordStore vaultPasswordStore = new VaultPasswordStore();

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
			setPassword(vaultPasswordStore.read());
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

	/**
	 * Read the password from the external secret manager when it is configured and not yet read: a failure stops the
	 * application context instead of the first decryption. The properties files are loaded at this point.
	 */
	@Override
	public void afterSingletonsInstantiated() {
		if (vaultPasswordStore.isEnabled()) {
			readVaultPassword();
		}
	}

	/**
	 * Replace the password in its source, when this source can be updated: the external secret manager when it is
	 * configured, otherwise the existing file the password is read from. The password given by a property, an
	 * environment variable or a classpath resource cannot be replaced.
	 *
	 * @param password The new password.
	 * @return The previous password of the updated source. <code>null</code> when the source has not been updated:
	 * the new password has to be provided to the next start by another way.
	 * @throws org.jasypt.exceptions.EncryptionInitializationException When the external secret manager is configured
	 *                                                                 and failed.
	 */
	public String store(final String password) {
		if (vaultPasswordStore.isEnabled()) {
			return vaultPasswordStore.write(password);
		}
		final var file = getPasswordFile();
		if (file != null) {
			try {
				final var previous = StringUtils.trimToEmpty(Files.readString(file));
				Files.writeString(file, password, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				return previous;
			} catch (final IOException ioe) {
				// Not a file of the file system, or not writable
				log.info("The master password file {} cannot be updated: {}", file, ioe.getClass().getSimpleName());
			}
		}
		return null;
	}

	/**
	 * Return the file the password is read from. <code>null</code> when the password comes from a raw value
	 * provider, or when there is no file provider.
	 */
	private Path getPasswordFile() {
		final var raw = Stream.<Supplier<String>>of(() -> pipe(passwordSysPropertyName, System::getProperty),
				() -> pipe(passwordSysPropertyName, GlobalPropertyUtils::getProperty), () -> pipe(passwordEnvName, System::getenv));
		if (raw.anyMatch(provider -> provider.get() != null)) {
			return null;
		}
		return Stream.<Supplier<String>>of(() -> pipe(passwordFilePropertyName, System::getProperty),
						() -> pipe(passwordFilePropertyName, GlobalPropertyUtils::getProperty), () -> pipe(passwordFileEnvName, System::getenv))
				.map(Supplier::get).filter(StringUtils::isNotBlank).filter(name -> getPasswordFromFile(name) != null).findFirst()
				.map(Path::of).orElse(null);
	}

	@Override
	public char[] getPasswordCharArray() {
		if (vaultPasswordStore.isEnabled()) {
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
