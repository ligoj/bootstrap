package org.ligoj.bootstrap.http.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.web.RedirectStrategy;

/**
 * Test {@link RedirectAuthenticationEntryPoint} implementation.
 */
public class RedirectAuthenticationEntryPointTest {

	private RedirectAuthenticationEntryPoint entryPoint;

	@Before
	public void setup() {
		entryPoint = new RedirectAuthenticationEntryPoint("http://h");
		final Set<String> redirectUrls = new HashSet<>();
		redirectUrls.add("/index.html");
		entryPoint.setRedirectUrls(redirectUrls);
	}

	@Test
	public void testNoRedirect() throws IOException, ServletException {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletPath()).thenReturn("/somethingelse");
		final RedirectStrategy strategy = Mockito.mock(RedirectStrategy.class);
		entryPoint.setRedirectStrategy(strategy);
		entryPoint.commence(request, null, null);
		Mockito.verify(strategy, Mockito.atLeastOnce()).sendRedirect(request, null, "");
	}

	@Test
	public void testRedirect() throws IOException, ServletException {
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletPath()).thenReturn("/index.html");
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.encodeRedirectURL("http://h")).thenReturn("encoded");
		entryPoint.commence(request, response, null);
		Mockito.verify(response, Mockito.atLeastOnce()).sendRedirect("encoded");
	}
}
