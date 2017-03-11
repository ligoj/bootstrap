package org.ligoj.bootstrap;

import javax.ws.rs.core.SecurityContext;

import org.junit.Assert;

import org.junit.Test;

/**
 * Test of {@link AbstractSecurityTest}
 */
public class TestAbstractSecurityTest extends AbstractSecurityTest {

	@Test
	public void testGetJaxRsSecurityContext() {
		final SecurityContext context = getJaxRsSecurityContext(getAuthenticationName());
		Assert.assertEquals(DEFAULT_USER, context.getUserPrincipal().getName());
	}

}
