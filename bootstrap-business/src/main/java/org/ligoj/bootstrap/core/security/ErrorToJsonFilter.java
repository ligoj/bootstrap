/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import lombok.extern.slf4j.Slf4j;

/**
 * Filter checking there is never a stack trace displayed in the response.
 */
@Component
@Slf4j
public class ErrorToJsonFilter extends GenericFilterBean {

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException {
		try {
			chain.doFilter(request, response);
		} catch (final Exception exception) {
			// Error before security/jax-rs management
			log.error("High level error", exception);
			
			// Catch this stack
			final var response3 = (HttpServletResponse) response;
			response3.setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
			response3.setContentType(MediaType.APPLICATION_JSON_TYPE.toString());
			response3.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response3.getOutputStream().write("{\"code\":\"internal\"}".getBytes(StandardCharsets.UTF_8));
			response3.flushBuffer();
		}

	}
}
