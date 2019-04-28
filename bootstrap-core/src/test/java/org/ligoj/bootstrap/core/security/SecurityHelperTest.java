/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * {@link SecurityHelper} test class.
 */
public class SecurityHelperTest {

	/**
	 * The system user name.
	 */
	public static final String SYSTEM_USERNAME = "_system";

	@BeforeEach
	public void setUp() {
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	public void cleanup() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * Test null user name.
	 */
	@Test
	public void setUserNameNull() {
		Assertions.assertNull(new SecurityHelper().setUserName(null));
	}

	/**
	 * Test user name.
	 */
	@Test
	public void setUserName() {
		final var sc = new SecurityHelper().setUserName("name");
		Assertions.assertEquals("name", sc.getAuthentication().getName());
		Assertions.assertEquals("name", new SecurityHelper().getLogin());
	}

	/**
	 * Test no login.
	 */
	@Test
	public void getLogin() {
		Assertions.assertNull(new SecurityHelper().getLogin());
	}

}
