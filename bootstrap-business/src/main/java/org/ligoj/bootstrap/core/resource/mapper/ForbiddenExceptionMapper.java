/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;

/**
 * Handles access denied exceptions like {@link ForbiddenException} to a JSON string.
 */
@Provider
public class ForbiddenExceptionMapper extends AbstractMapper implements ExceptionMapper<ForbiddenException> {

	@Override
	public Response toResponse(final ForbiddenException exception) {
		return toResponse(Status.FORBIDDEN, "security", null);
	}

}
