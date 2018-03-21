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
public class ExtendedSecurityExpressionHandlerTest {

	@Test
	public void testHasHeader() {
		FilterInvocation invocation = Mockito.mock(FilterInvocation.class);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn("value");
		Mockito.when(invocation.getRequest()).thenReturn(request);
		Assertions.assertTrue(((ExtendedWebSecurityExpressionRoot) new ExtendedSecurityExpressionHandler().createSecurityExpressionRoot(
				Mockito.mock(Authentication.class), invocation)).hasHeader("header"));
	}

	@Test
	public void testHasParameter() {
		FilterInvocation invocation = Mockito.mock(FilterInvocation.class);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("parameter")).thenReturn("value");
		Mockito.when(invocation.getRequest()).thenReturn(request);
		Assertions.assertTrue(((ExtendedWebSecurityExpressionRoot) new ExtendedSecurityExpressionHandler().createSecurityExpressionRoot(
				Mockito.mock(Authentication.class), invocation)).hasParameter("parameter"));
	}
}
