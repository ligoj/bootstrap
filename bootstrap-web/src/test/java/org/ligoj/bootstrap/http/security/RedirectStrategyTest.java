/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Custom redirection test of class {@link RestRedirectStrategy}
 */
class RedirectStrategyTest {

	@Test
    void sendRedirect() throws IOException {
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		final var redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(true);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
	}

	@Test
    void sendRedirectNotSuccess() throws IOException {
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		final var redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(false);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
	}

	@Test
    void sendRedirectNoCookie() throws IOException {
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		initSpringSecurityContext("user", Mockito.mock(Authentication.class));
		final var redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(true);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
	}

	@Test
    void sendRedirectCookies() throws IOException {
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		initSpringSecurityContext("user",
				new CookieUsernamePasswordAuthenticationToken("user", "N/A", null, Arrays.asList(new String[] { "key=value; path=/" })));
		final var redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(true);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
		Mockito.verify(response).addHeader("Set-Cookie", "key=value; path=/");
	}

	/**
	 * Initialize {@link SecurityContextHolder} for given user.
	 * 
	 * @param user
	 *            The user to set in the context.
	 * @param authentication
	 *            The optional current authentication.
	 * @return The configured {@link SecurityContext}.
	 */
	private SecurityContext initSpringSecurityContext(final String user, final Authentication authentication) {
		SecurityContextHolder.clearContext();
		final var context = Mockito.mock(SecurityContext.class);
		Mockito.when(context.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(context);
		return context;
	}

}
