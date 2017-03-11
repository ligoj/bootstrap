package org.ligoj.bootstrap.resource.system.crypto;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.core.crypto.CryptoHelper;
import org.ligoj.bootstrap.resource.system.security.CryptoResource;

/**
 * Test class of {@link CryptoResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/jpa-context-test.xml", "classpath:/META-INF/spring/business-context-test.xml",
		"classpath:/META-INF/spring/security-context-test.xml" })
@Rollback
@Transactional
public class CryptoResourceTest extends AbstractJpaTest {

	@Autowired
	private CryptoResource resource;

	@Autowired
	private CryptoHelper cryptoHelper;

	@BeforeClass
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");
	}

	@AfterClass
	public static void clean() {
		System.clearProperty("app.crypto.file");
	}

	@Before
	@After
	public void reset() {
		System.clearProperty("test.property");
	}

	/**
	 * test find all service
	 */
	@Test
	public void testFindAll() {
		final String encrypted = resource.create("value");
		Assert.assertNotEquals(encrypted, "value");
		Assert.assertEquals("value", cryptoHelper.decrypt(encrypted));

	}
}
