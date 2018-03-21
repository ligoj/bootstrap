/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link SystemEnvironmentAndFilePBEConfig} using a classpath resource file.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
public class SystemEnvironmentAndFilePBEConfigResourceTest extends AbstractSystemEnvironmentAndFilePBEConfigTest {

	@BeforeAll
	public static void init() {
		System.setProperty("app.crypto.file", "security.key");
	}

	@Test
	public void getPasswordFromFileClasspath() {
		Assertions.assertEquals("secret", new SystemEnvironmentAndFilePBEConfig().getPasswordFromFile("security.key"));
	}

	@Test
	public void getPasswordFromFileClasspathFailed() {
		Assertions.assertNull(new SystemEnvironmentAndFilePBEConfig().getPasswordFromFile("any.key"));
	}
}
