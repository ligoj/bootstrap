package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
