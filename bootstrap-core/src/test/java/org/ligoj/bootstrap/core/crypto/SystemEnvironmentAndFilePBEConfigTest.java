package org.ligoj.bootstrap.core.crypto;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class of {@link SystemEnvironmentAndFilePBEConfig} using a FS file.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
public class SystemEnvironmentAndFilePBEConfigTest extends AbstractSystemEnvironmentAndFilePBEConfigTest {

	@BeforeClass
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

}
