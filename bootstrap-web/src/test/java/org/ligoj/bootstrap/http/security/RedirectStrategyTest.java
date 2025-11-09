/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
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


	ByteArrayOutputStream sendRedirect(boolean success, String path, String url) throws IOException {
		final var response = Mockito.mock(HttpServletResponse.class);
		final var out = new ByteArrayOutputStream();
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(out));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		Mockito.when(request.getPathInfo()).thenReturn(path);
		final var redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(success);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, url);
		return out;
	}

	@Test
	void sendRedirect() throws IOException {
		var out = sendRedirect(true, "any", "");
		Assertions.assertEquals("{\"success\":true,\"redirect\":\"\"}", out.toString());
	}

	@Test
	void sendRedirectNotSuccessMessagesJS() throws IOException {
		var out = sendRedirect(false, "/messages.js", "");
		Assertions.assertEquals("define({root: {}})", out.toString());
	}

	@Test
	void sendRedirectNotSuccessJS() throws IOException {
		var out = sendRedirect(false, "/some.js", "");
		Assertions.assertEquals("errorManager?.handleRedirect('');", out.toString());
	}

	@Test
	void sendRedirectNotSuccessCSS() throws IOException {
		var out = sendRedirect(false, "/some.css", "");
		Assertions.assertEquals("", out.toString());
	}

	@Test
	void sendRedirectNotSuccessForce() throws IOException {
		var out = sendRedirect(false, "/some.js", null);
		Assertions.assertEquals("errorManager?.handleRedirect('');", out.toString());
	}

	@Test
	void sendRedirectNotSuccessJPG() throws IOException {
		var out = sendRedirect(false, "/some.jpg", "");
		Assertions.assertEquals("{\"success\":false,\"redirect\":\"\"}", out.toString());
	}

	@Test
	void sendRedirectNotSuccessHTML() throws IOException {
		var out = sendRedirect(false, "/some.html", "");
		Assertions.assertEquals("<div></div>", out.toString());
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
