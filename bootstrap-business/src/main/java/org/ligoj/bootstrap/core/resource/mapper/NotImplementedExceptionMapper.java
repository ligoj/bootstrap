/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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
