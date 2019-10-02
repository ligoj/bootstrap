/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
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
abstract class AbstractSystemEnvironmentAndFilePBEConfigTest {

	@Autowired
	private StringEncryptor encryptor;

	@Value("${app.test.simple}")
	private String simpleValue;

	@Value("${app.test.simpleCascaded}")
	private String simpleCascaded;

	@Value("${app.test.simpleEncrypted}")
	private String simpleEncrypted;

	@AfterAll
    static void clean() {
		System.clearProperty("app.crypto.file");
	}

	@BeforeEach
	@AfterEach
    void reset() {
		System.clearProperty("test.property");
	}

	@Test
    void encryptNotNull() {
		Assertions.assertNotNull(encryptor.encrypt("secret"));
	}

	@Test
    void encryptEncrypted() {
		Assertions.assertNotEquals("secret", encryptor.encrypt("secret"));
	}

	@Test
    void encryptSalted() {
		Assertions.assertNotEquals(encryptor.encrypt("secret"), encryptor.encrypt("secret"));
	}

	@Test
    void readDefault() {
		encryptor.encrypt("secret");
		Assertions.assertEquals("Simple Value", simpleValue);
		Assertions.assertEquals("Simple Value-cascaded", simpleCascaded);
		
		// Check encrypted data is read
		Assertions.assertEquals("secret", simpleEncrypted);
	}

	@Test
    void setPasswordSysPropertyNameGlobal() {
        var config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordSysPropertyName("app.test.lazy.password");
		Assertions.assertEquals("secret-spring", config.getPassword());
	}

	@Test
    void setPasswordFilePropertyNameGlobal() {
        var config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordFilePropertyName("app.test.lazy.file");
		Assertions.assertEquals("secret-spring2", config.getPassword());
	}

	@Test
    void setPasswordSysPropertyName() {
        var config = new SystemEnvironmentAndFilePBEConfig();
		System.setProperty("test.property", "-secret-");
		config.setPasswordSysPropertyName("test.property");
		Assertions.assertEquals("-secret-", config.getPassword());
	}

	@Test
    void setPasswordEnvName() {
        var config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordEnvName("PATH");
		Assertions.assertNotNull(config.getPassword());
	}

	@Test
    void setPasswordFileEnvName() {
        var config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordFileEnvName("PATH");
		Assertions.assertThrows(NullPointerException.class, config::getPassword);
	}

	@Test
    void setPasswordFilePropertyName() {
        var config = new SystemEnvironmentAndFilePBEConfig();
		System.setProperty("app.crypto.file", "-invalid-");
		config.setPasswordFilePropertyName("test.property");
		Assertions.assertThrows(NullPointerException.class, config::getPassword);
	}

}
