/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.springframework.security.core.AuthenticationException;

/**
 * Handles authentication exception to a JSON string.
 */
@Provider
public class AuthenticationExceptionMapper extends AbstractMapper implements ExceptionMapper<AuthenticationException> {

	@Override
	public Response toResponse(final AuthenticationException exception) {
		return toResponse(Status.UNAUTHORIZED, "security", exception);
	}

}
