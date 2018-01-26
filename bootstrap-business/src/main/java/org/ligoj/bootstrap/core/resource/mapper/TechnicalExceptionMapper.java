package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.resource.TechnicalException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles technical, but explicitly managed exception {@link TechnicalException} to a JSON string.
 */
@Provider
@Slf4j
public class TechnicalExceptionMapper extends AbstractMapper implements ExceptionMapper<TechnicalException> {

	@Override
	public Response toResponse(final TechnicalException exception) {
		log.error("Technical exception", exception);
		
		// Map to internal error without exposing the exception
		return toResponse(Status.INTERNAL_SERVER_ERROR, "technical", null);
	}

}
