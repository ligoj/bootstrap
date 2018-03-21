/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
