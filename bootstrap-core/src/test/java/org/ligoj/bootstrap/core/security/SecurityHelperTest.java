package org.ligoj.bootstrap.core.security;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * {@link SecurityHelper} test class.
 */
public class SecurityHelperTest {

	/**
	 * The system user name.
	 */
	public static final String SYSTEM_USERNAME = "_system";

	@Before
	public void setUp() {
		SecurityContextHolder.clearContext();
	}

	@After
	public void cleanup() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * Test null user name.
	 */
	@Test
	public void setUserNameNull() {
		Assert.assertNull(new SecurityHelper().setUserName(null));
	}

	/**
	 * Test user name.
	 */
	@Test
	public void setUserName() {
		final SecurityContext sc = new SecurityHelper().setUserName("name");
		Assert.assertEquals("name", sc.getAuthentication().getName());
		Assert.assertEquals("name", new SecurityHelper().getLogin());
	}

	/**
	 * Test no login.
	 */
	@Test
	public void getLogin() {
		Assert.assertNull(new SecurityHelper().getLogin());
	}

}
