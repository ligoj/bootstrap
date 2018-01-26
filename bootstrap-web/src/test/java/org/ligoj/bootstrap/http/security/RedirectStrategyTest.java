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
public class RedirectStrategyTest {

	@Test
	public void sendRedirect() throws IOException {
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		final RestRedirectStrategy redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(true);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
	}

	@Test
	public void sendRedirectNotSuccess() throws IOException {
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		final RestRedirectStrategy redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(false);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
	}

	@Test
	public void sendRedirectNoCookie() throws IOException {
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		initSpringSecurityContext("user", Mockito.mock(Authentication.class));
		final RestRedirectStrategy redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(true);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
	}

	@Test
	public void sendRedirectCookies() throws IOException {
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(new ByteArrayOutputStream()));
		Mockito.when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenReturn("");
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContextPath()).thenReturn("");
		initSpringSecurityContext("user",
				new CookieUsernamePasswordAuthenticationToken("user", "N/A", null, Arrays.asList(new String[] { "key=value; path=/" })));
		final RestRedirectStrategy redirectStrategy = new RestRedirectStrategy();
		redirectStrategy.setSuccess(true);
		redirectStrategy.setStatus(1);
		redirectStrategy.sendRedirect(request, response, "");
		Mockito.verify(response).addHeader("Set-Cookie", "key=value; path=/");
	}

	/**
	 * Initialize {@link SecurityContextHolder} for given user.
	 * 
	 * @param user
	 *            the user to set in the context.
	 * @param authorities
	 *            the optional authorities name
	 * @return The configured {@link SecurityContext}.
	 */
	private SecurityContext initSpringSecurityContext(final String user, final Authentication authentication) {
		SecurityContextHolder.clearContext();
		final SecurityContext context = Mockito.mock(SecurityContext.class);
		Mockito.when(context.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(context);
		return context;
	}

}
