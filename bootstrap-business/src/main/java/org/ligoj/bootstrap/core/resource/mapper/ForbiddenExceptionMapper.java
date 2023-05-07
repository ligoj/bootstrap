/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import org.ligoj.bootstrap.core.resource.AbstractMapper;

/**
 * Handles access denied exceptions like {@link ForbiddenException} to a JSON string.
 */
@Provider
@Slf4j
public class ForbiddenExceptionMapper extends AbstractMapper implements ExceptionMapper<ForbiddenException> {

	@Override
	public Response toResponse(final ForbiddenException exception) {
		log.debug("Forbidden access for user", exception);
		return toResponse(Status.FORBIDDEN, "security", null);
	}

}
