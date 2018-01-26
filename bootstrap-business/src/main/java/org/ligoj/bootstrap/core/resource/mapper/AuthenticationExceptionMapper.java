package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
