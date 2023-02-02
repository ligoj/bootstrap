/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.springframework.security.access.AccessDeniedException;

/**
 * Handles access denied exceptions like {@link AccessDeniedException} to a JSON string.
 */
@Provider
public class AccessDeniedExceptionMapper extends AbstractMapper implements ExceptionMapper<AccessDeniedException> {

	@Override
	public Response toResponse(final AccessDeniedException exception) {
		return toResponse(Status.FORBIDDEN, "security", null);
	}

}
