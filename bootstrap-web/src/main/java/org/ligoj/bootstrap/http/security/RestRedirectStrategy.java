/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;

import lombok.Setter;

/**
 * This strategy replace the standard 302 code by a simple JSON data since the client is an hidden Ajax thread. More
 * information could be added later in the JSON stream.
 */
public class RestRedirectStrategy implements RedirectStrategy {

	/**
	 * Failure redirection mode. Default is true.
	 */
	@Setter
	private boolean success;

	/**
	 * Status to use.
	 */
	@Setter
	private int status = HttpServletResponse.SC_OK;

	@Override
	public void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, final String url) throws IOException {

		// The status is OK, not MOVED
		response.setStatus(status);

		// Write the UTF-8 JSON content
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType("application/json");

		final var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (success && authentication instanceof CookieUsernamePasswordAuthenticationToken cAuth) {
			// Forward cookies from back-office
			cAuth.getCookies().forEach(cookie -> response.addHeader("Set-Cookie", cookie));
		}
		// Write the JSON data containing the redirection and the status
		final var redirectUrl = response.encodeRedirectURL(request.getContextPath() + url);
		IOUtils.write(String.format("{\"success\":%b,\"redirect\":\"%s\"}", success, redirectUrl), response.getOutputStream(),
				StandardCharsets.UTF_8);
	}

}
