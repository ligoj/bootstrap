package org.ligoj.bootstrap.core.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

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
			final HttpServletResponse response3 = (HttpServletResponse) response;
			response3.setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
			response3.setContentType(MediaType.APPLICATION_JSON_TYPE.toString());
			response3.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response3.getOutputStream().write("{\"code\":\"internal\"}".getBytes(StandardCharsets.UTF_8));
			response3.flushBuffer();
		}

	}
}
