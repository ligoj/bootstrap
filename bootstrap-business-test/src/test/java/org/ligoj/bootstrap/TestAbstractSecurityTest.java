package org.ligoj.bootstrap;

import javax.ws.rs.core.SecurityContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of {@link AbstractSecurityTest}
 */
public class TestAbstractSecurityTest extends AbstractSecurityTest {

	@Test
	public void testGetJaxRsSecurityContext() {
		final SecurityContext context = getJaxRsSecurityContext(getAuthenticationName());
		Assertions.assertEquals(DEFAULT_USER, context.getUserPrincipal().getName());
	}

}
