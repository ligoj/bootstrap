package org.ligoj.bootstrap.core;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Check spring utilities.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/application-context-test.xml" })
public class SpringUtilsTest {

	@Test
	public void getApplicationContext() {
		Assert.assertNotNull(SpringUtils.getApplicationContext());
	}

	@Test
	public void testApplicationContext2() {
		Assert.assertNotNull(SpringUtils.getBean(org.jasypt.encryption.pbe.StandardPBEStringEncryptor.class));
	}

}
