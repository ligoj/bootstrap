/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Custom redirection test of class {@link RestRedirectStrategy}
 */
class RedirectStrategyTest {


	void sendRedirect(boolean success, String path, String url) throws IOException {
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		Mockito.when(request.getPathInfo()).thenReturn(path);
		final var redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(success);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, url);
	}

	@Test
	void sendRedirect() throws IOException {
		sendRedirect(true, "any", "");
	}

	@Test
	void sendRedirectNotSuccessMessagesJS() throws IOException {
		sendRedirect(false, "/messages.js", "");
	}

	@Test
	void sendRedirectNotSuccessJS() throws IOException {
		sendRedirect(false, "/some.js", "");
	}

	@Test
	void sendRedirectNotSuccessForce() throws IOException {
		sendRedirect(false, "/some.js", null);
	}

	@Test
	void sendRedirectNotSuccessJPG() throws IOException {
		sendRedirect(false, "/some.jpg", "");
	}

	@Test
	void sendRedirectNotSuccessHTML() throws IOException {
		sendRedirect(false, "/some.html", "");
	}

	@Test
	void sendRedirectNoCookie() throws IOException {
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		Mockito.when(request.getPathInfo()).thenReturn("");
		initSpringSecurityContext(Mockito.mock(Authentication.class));
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
		Mockito.when(request.getPathInfo()).thenReturn("");
		initSpringSecurityContext(
				new CookieUsernamePasswordAuthenticationToken("user", "N/A", null, List.of("key=value; path=/")));
		final var redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(true);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
		Mockito.verify(response).addHeader("Set-Cookie", "key=value; path=/");
	}

	/**
	 * Initialize {@link SecurityContextHolder} for given user.
	 *
	 * @param authentication The optional current authentication.
	 */
	private void initSpringSecurityContext(final Authentication authentication) {
		SecurityContextHolder.clearContext();
		final var context = Mockito.mock(SecurityContext.class);
		Mockito.when(context.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(context);
	}

}
