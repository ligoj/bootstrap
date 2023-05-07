/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.springframework.security.access.AccessDeniedException;

/**
 * Handles access denied exceptions like {@link AccessDeniedException} to a JSON string.
 */
@Provider
@Slf4j
public class AccessDeniedExceptionMapper extends AbstractMapper implements ExceptionMapper<AccessDeniedException> {

	@Override
	public Response toResponse(final AccessDeniedException exception) {
		log.debug("Denied access for user", exception);
		return toResponse(Status.FORBIDDEN, "security", null);
	}

}
