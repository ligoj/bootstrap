/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.web.RedirectStrategy;

/**
 * Test {@link RedirectAuthenticationEntryPoint} implementation.
 */
class RedirectAuthenticationEntryPointTest {

	private RedirectAuthenticationEntryPoint entryPoint;

	@BeforeEach
    void setup() {
		entryPoint = new RedirectAuthenticationEntryPoint("http://h");
		final Set<String> redirectUrls = new HashSet<>();
		redirectUrls.add("/index.html");
		entryPoint.setRedirectUrls(redirectUrls);
	}

	@Test
    void testNoRedirect() throws IOException, ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletPath()).thenReturn("/something-else");
		final var strategy = Mockito.mock(RedirectStrategy.class);
		entryPoint.setRedirectStrategy(strategy);
		entryPoint.commence(request, null, null);
		Mockito.verify(strategy, Mockito.atLeastOnce()).sendRedirect(request, null, "");
	}

	@Test
    void testRedirect() throws IOException, ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getServletPath()).thenReturn("/index.html");
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.encodeRedirectURL("http://h")).thenReturn("encoded");
		entryPoint.commence(request, response, null);
		Mockito.verify(response, Mockito.atLeastOnce()).sendRedirect("encoded");
	}
}
