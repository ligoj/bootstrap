package org.ligoj.bootstrap.core.crypto;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class of {@link SystemEnvironmentAndFilePBEConfig} using a classpath resource file.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
public class SystemEnvironmentAndFilePBEConfigResourceTest extends AbstractSystemEnvironmentAndFilePBEConfigTest {

	@BeforeClass
	public static void init() {
		System.setProperty("app.crypto.file", "security.key");
	}

	@Test
	public void getPasswordFromFileClasspath() {
		Assert.assertEquals("secret", new SystemEnvironmentAndFilePBEConfig().getPasswordFromFile("security.key"));
	}

	@Test
	public void getPasswordFromFileClasspathFailed() {
		Assert.assertNull(new SystemEnvironmentAndFilePBEConfig().getPasswordFromFile("any.key"));
	}
}
