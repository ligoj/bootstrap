package org.ligoj.bootstrap.core.security;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;

/**
 * Test class of {@link ApiTokenAuthenticationFilter}
 */
public class ApiTokenAuthenticationFilterTest extends AbstractJpaTest {

	private ApiTokenAuthenticationFilter filter;

	private ApiTokenResource resource;

	@Before
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
		Assert.assertEquals("A", filter.getPreAuthenticatedCredentials(request));
	}

	@Test
	public void testNoCredential() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Assert.assertEquals("N/A", filter.getPreAuthenticatedCredentials(request));
	}

	@Test(expected = PreAuthenticatedCredentialsNotFoundException.class)
	public void testPrincipalHeaderMissing() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		filter.setExceptionIfHeaderMissing(true);
		Assert.assertNull(filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	public void testNoPrincipal() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Assert.assertNull(filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	public void testPrincipal() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		Assert.assertEquals(DEFAULT_USER, filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	public void testPrincipalWithCredentialAsToken() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		Mockito.when(request.getHeader("credential")).thenReturn("RFYG");
		Mockito.when(resource.check(DEFAULT_USER, "RFYG")).thenReturn(true);
		Assert.assertEquals(DEFAULT_USER, filter.getPreAuthenticatedPrincipal(request));
	}

	@Test
	public void testInvalidToken() {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("principal")).thenReturn(DEFAULT_USER);
		Mockito.when(request.getHeader("credential")).thenReturn("RFYG");
		Assert.assertNull(filter.getPreAuthenticatedPrincipal(request));
	}
}
