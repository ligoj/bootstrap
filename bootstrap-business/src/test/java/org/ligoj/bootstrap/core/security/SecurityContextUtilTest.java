/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Class to test the securityContexUtilClass
 */
class SecurityContextUtilTest {

	private final SecurityHelper securityHelper = new SecurityHelper();

	/**
	 * Name for the user authenticated
	 */
	private static final String USER_NAME = "admin";

	/**
	 * Method to test the mock authentication test
	 */
	@Test
	void testSetUserNameNoName() {
		// call to the method
		securityHelper.setUserName(USER_NAME);
		securityHelper.setUserName(null);

		// make the assertion of untouched username
		final var nameAuthenticated = SecurityContextHolder.getContext().getAuthentication().getName();
		Assertions.assertEquals(USER_NAME, nameAuthenticated);
	}

	/**
	 * Method to test the mock authentication test
	 */
	@Test
	void testSetUserName() {
		// call to the method
		securityHelper.setUserName(USER_NAME);

		// make the assertion
		final var nameAuthenticated = SecurityContextHolder.getContext().getAuthentication().getName();
		Assertions.assertEquals(USER_NAME, nameAuthenticated);
	}

}
