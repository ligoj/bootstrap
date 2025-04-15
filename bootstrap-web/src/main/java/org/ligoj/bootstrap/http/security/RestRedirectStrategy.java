/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This strategy replace the standard 302 code by a simple JSON data since the client is a hidden Ajax thread. More
 * information could be added later in the JSON stream.
 */
@Setter
public class RestRedirectStrategy implements RedirectStrategy {

	/**
	 * Failure redirection mode. Default is true.
	 */
	private boolean success;

	/**
	 * Status to use.
	 */
	private int status = HttpServletResponse.SC_OK;

	private Map<String, String> EXTENSION_TO_MIME = Map.of("js", "text/javascript", "html", "text/html", "css", "text/css");

	@Override
	public void sendRedirect(final HttpServletRequest request, final HttpServletResponse response, final String url) throws IOException {
		final var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (success && authentication instanceof CookieUsernamePasswordAuthenticationToken cAuth) {
			// Forward cookies from back-office
			cAuth.getCookies().forEach(cookie -> response.addHeader("Set-Cookie", cookie));
		}

		final var extension = FilenameUtils.getExtension(request.getPathInfo());
		final var mime = EXTENSION_TO_MIME.get(extension);
		// Write the JSON data containing the redirection and the status
		final var redirect = url == null ? response.encodeRedirectURL(request.getContextPath()) : "local";
		response.setStatus(mime == null ? status : HttpServletResponse.SC_OK);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(mime == null ? "application/json" : mime);
		response.setHeader("x-redirect", redirect);
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "0");
		if (request.getPathInfo().endsWith("/messages.js")) {
			IOUtils.write("define({root: {}})", response.getOutputStream(), StandardCharsets.UTF_8);
		} else if (mime == null) {
			IOUtils.write(String.format("{\"success\":%b,\"redirect\":\"%s\"}", success, redirect), response.getOutputStream(),
					StandardCharsets.UTF_8);
		} else if (mime.equals("text/javascript")) {
			IOUtils.write(String.format("errorManager?.handleRedirect('%s');", redirect), response.getOutputStream(), StandardCharsets.UTF_8);
		}
	}

}
