package org.ligoj.bootstrap.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Check spring utilities.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/application-context-test.xml" })
public class SpringUtilsTest {

	@BeforeAll
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

	@Test
	public void getApplicationContext() {
		Assertions.assertNotNull(SpringUtils.getApplicationContext());
	}

	@Test
	public void testApplicationContext2() {
		Assertions.assertNotNull(SpringUtils.getBean(org.jasypt.encryption.pbe.StandardPBEStringEncryptor.class));
	}

}
