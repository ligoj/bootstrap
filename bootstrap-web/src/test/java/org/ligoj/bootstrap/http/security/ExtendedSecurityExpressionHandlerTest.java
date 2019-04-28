/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

/**
 * Test class of {@link ExtendedSecurityExpressionHandler}
 */
class ExtendedSecurityExpressionHandlerTest {

	@Test
    void testHasHeader() {
        var invocation = Mockito.mock(FilterInvocation.class);
        var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn("value");
		Mockito.when(invocation.getRequest()).thenReturn(request);
		Assertions.assertTrue(((ExtendedWebSecurityExpressionRoot) new ExtendedSecurityExpressionHandler().createSecurityExpressionRoot(
				Mockito.mock(Authentication.class), invocation)).hasHeader("header"));
	}

	@Test
    void testHasParameter() {
        var invocation = Mockito.mock(FilterInvocation.class);
        var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("parameter")).thenReturn("value");
		Mockito.when(invocation.getRequest()).thenReturn(request);
		Assertions.assertTrue(((ExtendedWebSecurityExpressionRoot) new ExtendedSecurityExpressionHandler().createSecurityExpressionRoot(
				Mockito.mock(Authentication.class), invocation)).hasParameter("parameter"));
	}
}
