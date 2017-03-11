package org.ligoj.bootstrap.core.crypto;

import org.jasypt.encryption.StringEncryptor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

	@AfterClass
	public static void clean() {
		System.clearProperty("app.crypto.file");
	}

	@Before
	@After
	public void reset() {
		System.clearProperty("test.property");
	}

	@Test
	public void encryptNotNull() {
		Assert.assertNotNull(encryptor.encrypt("secret"));
	}

	@Test
	public void encryptEncrypted() {
		Assert.assertNotEquals("secret", encryptor.encrypt("secret"));
	}

	@Test
	public void encryptSalted() {
		Assert.assertNotEquals(encryptor.encrypt("secret"), encryptor.encrypt("secret"));
	}

	@Test
	public void readDefault() {
		encryptor.encrypt("secret");
		Assert.assertEquals("Simple Value", simpleValue);
		Assert.assertEquals("Simple Value-cascaded", simpleCascaded);
		Assert.assertEquals("secret", simpleEncrypted);
	}

	@Test
	public void setPasswordSysPropertyNameGlobal() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordSysPropertyName("app.test.lazy.password");
		Assert.assertEquals("secret-spring", config.getPassword());
	}

	@Test
	public void setPasswordFilePropertyNameGlobal() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordFilePropertyName("app.test.lazy.file");
		Assert.assertEquals("secret-spring2", config.getPassword());
	}

	@Test
	public void setPasswordSysPropertyName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		System.setProperty("test.property", "-secret-");
		config.setPasswordSysPropertyName("test.property");
		Assert.assertEquals("-secret-", config.getPassword());
	}

	@Test
	public void setPasswordEnvName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordEnvName("PATH");
		Assert.assertNotNull(config.getPassword());
	}

	@Test(expected = NullPointerException.class)
	public void setPasswordFileEnvName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		config.setPasswordFileEnvName("PATH");
		config.getPassword();
	}

	@Test(expected = NullPointerException.class)
	public void setPasswordFilePropertyName() {
		SystemEnvironmentAndFilePBEConfig config = new SystemEnvironmentAndFilePBEConfig();
		System.setProperty("app.crypto.file", "-invalid-");
		config.setPasswordFilePropertyName("test.property");
		config.getPassword();
	}

}
