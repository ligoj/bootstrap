/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;
import org.mockito.Mockito;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

/**
 * Test class of {@link ApiTokenAuthenticationFilter}
 */
public class ApiTokenAuthenticationFilterTest extends AbstractJpaTest {

	private ApiTokenAuthenticationFilter filter;

	private ApiTokenResource resource;

	@BeforeEach
	public void initializeData() {
		filter = new ApiTokenAuthenticationFilter();
		filter.setCredentialsRequestHeader("credential");
		filter.setPrincipalRequestHeader("principal");
		filter.setExceptionIfHeaderMissing(false);
		resource = Mockito.mock(ApiTokenResource.class);
		filter.setResource(resource);
	}

	@Test
	public void testCredential() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("credential")).thenReturn("A");
		Assertions.assertEquals("A", filter.getPreAuthenticatedCredentials(request));
	}

	@Test
	public void testNoCredential() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Assertions.assertEquals("N/A", filter.getPreAuthenticatedCredentials(request));
	}

	@Test
	public void testPrincipalHeaderMissing() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		filter.setExceptionIfHeaderMissing(true);
		Assertions.assertThrows(PreAuthenticatedCredentialsNotFoundException.class, () -> {
			filter.getPreAuthenticatedPrincipal(request);
		});
	}

	@Test
	public void testNoPrincipal() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Assertions.assertNull(filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	public void testPrincipal() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		Assertions.assertEquals(DEFAULT_USER, filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	public void testPrincipalWithCredentialAsToken() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		Mockito.when(request.getHeader("credential")).thenReturn("RFYG");
		Mockito.when(resource.check(DEFAULT_USER, "RFYG")).thenReturn(true);
		Assertions.assertEquals(DEFAULT_USER, filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	public void testInvalidToken() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		Mockito.when(request.getHeader("credential")).thenReturn("RFYG");
		Assertions.assertNull(filter.getPreAuthenticatedPrincipal(request));
	}
}
