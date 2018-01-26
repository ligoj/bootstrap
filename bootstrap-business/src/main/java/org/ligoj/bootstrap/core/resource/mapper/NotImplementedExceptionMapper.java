package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.NotImplementedException;
import org.ligoj.bootstrap.core.resource.AbstractMapper;

/**
 * Maps a {@link NotImplementedException} to a JSR-303 validation error. Status code, and contents
 * are updated.
 */
@Provider
public class NotImplementedExceptionMapper extends AbstractMapper implements ExceptionMapper<NotImplementedException> {

	@Override
	public Response toResponse(final NotImplementedException ex) {
		return toResponse(Status.NOT_IMPLEMENTED, "not-implemented", ex);
	}
}
