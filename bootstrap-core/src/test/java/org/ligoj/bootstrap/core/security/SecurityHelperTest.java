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
class SecurityHelperTest {

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void cleanup() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * Test null username.
	 */
	@Test
	void setUserNameNull() {
		Assertions.assertNull(new SecurityHelper().setUserName(null));
	}

	/**
	 * Test username.
	 */
	@Test
	void setUserName() {
		final var sc = new SecurityHelper().setUserName("name");
		Assertions.assertEquals("name", sc.getAuthentication().getName());
		Assertions.assertEquals("name", new SecurityHelper().getLogin());
	}

	/**
	 * Test no login.
	 */
	@Test
	void getLogin() {
		Assertions.assertNull(new SecurityHelper().getLogin());
	}

}
