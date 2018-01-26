package org.ligoj.bootstrap.core.crypto;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Test class of {@link SystemEnvironmentAndFilePBEConfig}
 */
public abstract class AbstractSystemEnvironmentAndFilePBEConfigTest {

	@Autowired
	private StringEncryptor encryptor;

	@Value("${app.test.simple}")
	private String simpleValue;

	@Value("${app.test.simpleCascaded}")
	private String simpleCascaded;

	@Value("${app.test.simpleEncrypted}")
	private String simpleEncrypted;

	@AfterAll
	public static void clean() {
		System.clearProperty("app.crypto.file");
	}

	@BeforeEach
	@AfterEach
	public void reset() {
		System.clearProperty("test.property");
	}

	@Test
	public void encryptNotNull() {
		Assertions.assertNotNull(encryptor.encrypt("secret"));
	}

	@Test
	public void encryptEncrypted() {
		Assertions.assertNotEquals("secret", encryptor.encrypt("secret"));
	}

	@Test
	public void encryptSalted() {
		Assertions.assertNotEquals(encryptor.encrypt("secret"), encryptor.encrypt("secret"));
	}

	@Test
	public void readDefault() {
		encryptor.encrypt("secret");
		Assertions.assertEquals("Simple Value", simpleValue);
		Assertions.assertEquals("Simple Value-cascaded", simpleCascaded);
		Assertions.assertEquals("secret", simpleEncrypted);
	}

	@Test
	public void setPasswordSysPropertyNameGlobal() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordSysPropertyName("app.test.lazy.password");
		Assertions.assertEquals("secret-spring", config.getPassword());
	}

	@Test
	public void setPasswordFilePropertyNameGlobal() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordFilePropertyName("app.test.lazy.file");
		Assertions.assertEquals("secret-spring2", config.getPassword());
	}

	@Test
	public void setPasswordSysPropertyName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		System.setProperty("test.property", "-secret-");
		config.setPasswordSysPropertyName("test.property");
		Assertions.assertEquals("-secret-", config.getPassword());
	}

	@Test
	public void setPasswordEnvName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordEnvName("PATH");
		Assertions.assertNotNull(config.getPassword());
	}

	@Test
	public void setPasswordFileEnvName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordFileEnvName("PATH");
		Assertions.assertThrows(NullPointerException.class, () -> {
			config.getPassword();
		});
	}

	@Test
	public void setPasswordFilePropertyName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		System.setProperty("app.crypto.file", "-invalid-");
		config.setPasswordFilePropertyName("test.property");
		Assertions.assertThrows(NullPointerException.class, () -> {
			config.getPassword();
		});
	}

}
