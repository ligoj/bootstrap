/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import com.sun.net.httpserver.HttpServer;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test class of {@link SystemEnvironmentAndFilePBEConfig} using {@link VaultPasswordStore}.
 */
class SystemEnvironmentAndFilePBEConfigVaultTest {

	private static final String PASSWORD_PROPERTY = "app.crypto.password";
	private static final String FILE_PROPERTY = "app.crypto.file";

	private HttpServer server;
	private final AtomicInteger requests = new AtomicInteger();
	private int status = 200;
	private final Map<String, String> env = new HashMap<>();
	private SystemEnvironmentAndFilePBEConfig config;
	private String previousFile;

	@BeforeEach
	void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/v1/secret/data/ligoj", exchange -> {
			requests.incrementAndGet();
			final var bytes = "{\"data\":{\"data\":{\"password\":\"fromVault\"}}}".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(status, bytes.length);
			exchange.getResponseBody().write(bytes);
			exchange.close();
		});
		server.start();

		// The other tests of this module define this property
		previousFile = System.clearProperty(FILE_PROPERTY);

		config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordSysPropertyName(PASSWORD_PROPERTY);
		config.setPasswordFilePropertyName(FILE_PROPERTY);
		config.setPasswordEnvName("APP_CRYPTO_PASSWORD_TEST_UNDEFINED");
		config.setPasswordFileEnvName("APP_CRYPTO_FILE_TEST_UNDEFINED");
		config.setVaultPasswordStore(new VaultPasswordStore(env::get));
	}

	@AfterEach
	void tearDown() {
		server.stop(0);
		System.clearProperty(PASSWORD_PROPERTY);
		if (previousFile == null) {
			System.clearProperty(FILE_PROPERTY);
		} else {
			System.setProperty(FILE_PROPERTY, previousFile);
		}
	}

	private void enableVault() {
		env.put("APP_CRYPTO_VAULT_URL", "http://localhost:" + server.getAddress().getPort());
		env.put("APP_CRYPTO_VAULT_PATH", "secret/data/ligoj");
		env.put("APP_CRYPTO_VAULT_TOKEN", "t0ken");
	}

	@Test
	void vaultDisabled() {
		System.setProperty(PASSWORD_PROPERTY, "local");
		Assertions.assertEquals("local", config.getPassword());
		Assertions.assertEquals(0, requests.get());
	}

	@Test
	void vaultDisabledByDefault() {
		System.setProperty(PASSWORD_PROPERTY, "local");
		final var defaultConfig = new SystemEnvironmentAndFilePBEConfig();
		defaultConfig.setPasswordSysPropertyName(PASSWORD_PROPERTY);
		Assertions.assertEquals("local", defaultConfig.getPassword());
	}

	@Test
	void vaultEnabled() {
		enableVault();
		Assertions.assertEquals("fromVault", config.getPassword());
	}

	@Test
	void vaultIsExclusive() {
		enableVault();
		System.setProperty(PASSWORD_PROPERTY, "local");
		System.setProperty(FILE_PROPERTY, "src/test/resources/security.key");
		Assertions.assertEquals("fromVault", config.getPassword());
	}

	@Test
	void vaultReadOnce() {
		enableVault();
		Assertions.assertEquals("fromVault", config.getPassword());
		Assertions.assertEquals("fromVault", config.getPassword());
		Assertions.assertArrayEquals("fromVault".toCharArray(), config.getPasswordCharArray());
		Assertions.assertEquals(1, requests.get());
	}

	@Test
	void vaultFailsClosed() {
		enableVault();
		status = 500;
		System.setProperty(PASSWORD_PROPERTY, "local");
		Assertions.assertThrows(EncryptionInitializationException.class, config::getPassword);
		Assertions.assertThrows(EncryptionInitializationException.class, config::getPassword);
	}

	@Test
	void vaultReadAtStartup() {
		enableVault();
		config.afterSingletonsInstantiated();
		Assertions.assertEquals(1, requests.get());
		Assertions.assertEquals("fromVault", config.getPassword());
		Assertions.assertEquals(1, requests.get());
	}

	@Test
	void vaultFailsAtStartup() {
		enableVault();
		status = 500;
		Assertions.assertThrows(EncryptionInitializationException.class, config::afterSingletonsInstantiated);
	}

	@Test
	void vaultDisabledAtStartup() {
		config.afterSingletonsInstantiated();
		Assertions.assertEquals(0, requests.get());
	}

	@Test
	void vaultFailureStopsTheContext() {
		enableVault();
		status = 500;
		try (var context = new GenericApplicationContext()) {
			context.registerBean("environmentVariablesConfiguration", SystemEnvironmentAndFilePBEConfig.class, () -> config);
			final var failure = Assertions.assertThrows(EncryptionInitializationException.class, context::refresh);
			Assertions.assertTrue(failure.getMessage().endsWith(": HTTP 500"), failure.getMessage());
		}
	}

	@Test
	void storeVault() {
		final var store = Mockito.mock(VaultPasswordStore.class);
		Mockito.when(store.isEnabled()).thenReturn(true);
		Mockito.when(store.write("new")).thenReturn("previous");
		config.setVaultPasswordStore(store);
		Assertions.assertEquals("previous", config.store("new"));
	}

	@Test
	void storeFile(@TempDir final Path directory) throws IOException {
		final var file = directory.resolve("security.key");
		Files.writeString(file, " old \n");
		System.setProperty(FILE_PROPERTY, file.toString());
		Assertions.assertEquals("old", config.getPassword());
		Assertions.assertEquals("old", config.store("new"));
		Assertions.assertEquals("new", Files.readString(file));
		Assertions.assertEquals("new", config.store("old"));
		Assertions.assertEquals("old", Files.readString(file));
	}

	@Test
	void storeNotWritableFile(@TempDir final Path directory) throws IOException {
		final var file = directory.resolve("security.key");
		Files.writeString(file, "old");
		Assertions.assertTrue(file.toFile().setReadOnly());
		System.setProperty(FILE_PROPERTY, file.toString());
		Assertions.assertNull(config.store("new"));
		Assertions.assertEquals("old", Files.readString(file));
	}

	@Test
	void storeClasspathResource() {
		System.setProperty(FILE_PROPERTY, "security.key");
		Assertions.assertNotNull(config.getPassword());
		Assertions.assertNull(config.store("new"));
		Assertions.assertFalse(Files.exists(Path.of("security.key")));
	}

	@Test
	void storeMissingFile() {
		System.setProperty(FILE_PROPERTY, "target/not-found/security.key");
		Assertions.assertNull(config.store("new"));
	}

	@Test
	void storeBlankFileName() {
		System.setProperty(FILE_PROPERTY, " ");
		Assertions.assertNull(config.store("new"));
	}

	@Test
	void storeRawPassword(@TempDir final Path directory) throws IOException {
		final var file = directory.resolve("security.key");
		Files.writeString(file, "old");
		System.setProperty(FILE_PROPERTY, file.toString());
		System.setProperty(PASSWORD_PROPERTY, "local");
		Assertions.assertNull(config.store("new"));
		Assertions.assertEquals("old", Files.readString(file));
	}

	@Test
	void storeNoSource() {
		Assertions.assertNull(config.store("new"));
	}

	@Test
	void vaultCustomReader() {
		final var reader = Mockito.mock(VaultPasswordStore.class);
		Mockito.when(reader.isEnabled()).thenReturn(true);
		Mockito.when(reader.read()).thenReturn("mocked");
		config.setVaultPasswordStore(reader);
		Assertions.assertEquals("mocked", config.getPassword());
	}
}
