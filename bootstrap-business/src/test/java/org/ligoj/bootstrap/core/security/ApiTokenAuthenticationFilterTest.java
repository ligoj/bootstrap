/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class of {@link ApiTokenAuthenticationFilter}
 */
class ApiTokenAuthenticationFilterTest extends AbstractJpaTest {

	private ApiTokenAuthenticationFilter filter;

	private ApiTokenResource resource;

	@BeforeEach
	void initializeData() {
		filter = new ApiTokenAuthenticationFilter();
		filter.setCredentialsRequestHeader("credential");
		filter.setPrincipalRequestHeader("principal");
		filter.setExceptionIfHeaderMissing(false);
		resource = mock(ApiTokenResource.class);
		filter.setResource(resource);
	}

	@Test
	void testCredential() {
		final var request = mock(HttpServletRequest.class);
		when(request.getHeader("credential")).thenReturn("A");
		Assertions.assertEquals("A", filter.getPreAuthenticatedCredentials(request));
	}

	@Test
	void testNoCredential() {
		final var request = mock(HttpServletRequest.class);
		Assertions.assertEquals("N/A", filter.getPreAuthenticatedCredentials(request));
	}

	@Test
	void testPrincipalHeaderMissing() {
		final var request = mock(HttpServletRequest.class);
		filter.setExceptionIfHeaderMissing(true);
		Assertions.assertThrows(PreAuthenticatedCredentialsNotFoundException.class, () -> filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	void testNoPrincipal() {
		final var request = mock(HttpServletRequest.class);
		Assertions.assertNull(filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	void testPrincipalWithoutKey() {
		final var request = mock(HttpServletRequest.class);
		when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		Assertions.assertEquals(DEFAULT_USER, filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	void testPrincipalWithCredentialAsToken() {
		final var request = mock(HttpServletRequest.class);
		when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		when(request.getHeader("credential")).thenReturn("SECRET");
		when(resource.check(DEFAULT_USER, "SECRET")).thenReturn(true);
		Assertions.assertEquals(DEFAULT_USER, filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	void testPrincipalWithCredentialAsTokenAndVia() {
		final var request = mock(HttpServletRequest.class);
		when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		when(request.getHeader("credential")).thenReturn("SECRET");
		when(request.getHeader("x-api-via-user")).thenReturn("admin");
		when(resource.check("admin", "SECRET")).thenReturn(true);
		Assertions.assertEquals(DEFAULT_USER, filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	void testInvalidToken() {
		final var request = mock(HttpServletRequest.class);
		when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		when(request.getHeader("credential")).thenReturn("SECRET");
		Assertions.assertNull(filter.getPreAuthenticatedPrincipal(request));
	}
}
