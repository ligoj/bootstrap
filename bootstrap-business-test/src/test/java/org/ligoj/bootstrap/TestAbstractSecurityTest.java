/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.security.RbacUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Test of {@link AbstractSecurityTest}
 */
class TestAbstractSecurityTest extends AbstractSecurityTest {

	@Test
	void testGetJaxRsSecurityContext() {
		final var context = getJaxRsSecurityContext(getAuthenticationName());
		Assertions.assertEquals(DEFAULT_USER, context.getUserPrincipal().getName());
	}

	@Test
	void testInitSpringSecurityContext() {
		final var context = initSpringSecurityContext(DEFAULT_USER, new SimpleGrantedAuthority(DEFAULT_ROLE));
		Assertions.assertEquals(DEFAULT_USER, context.getAuthentication().getName());
	}

	@Test
	void testInitSpringSecurityContextAdmin() {
		final var context = initSpringSecurityContextAdmin(DEFAULT_USER, new SimpleGrantedAuthority(DEFAULT_ROLE));
		Assertions.assertEquals(DEFAULT_USER, context.getAuthentication().getName());
		Assertions.assertTrue(((RbacUserDetails)context.getAuthentication().getPrincipal()).isAdmin());
	}

}
