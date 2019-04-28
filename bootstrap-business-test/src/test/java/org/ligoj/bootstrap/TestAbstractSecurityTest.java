/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of {@link AbstractSecurityTest}
 */
public class TestAbstractSecurityTest extends AbstractSecurityTest {

	@Test
	void testGetJaxRsSecurityContext() {
		final var context = getJaxRsSecurityContext(getAuthenticationName());
		Assertions.assertEquals(DEFAULT_USER, context.getUserPrincipal().getName());
	}

}
