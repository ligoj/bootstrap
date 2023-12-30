/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles all CXF level errors.
 */
@Provider
@Slf4j
public class WebApplicationExceptionMapper extends AbstractMapper implements ExceptionMapper<WebApplicationException> {

	@Override
	public Response toResponse(final WebApplicationException exception) {
		if (exception.getResponse().getStatus() != Status.NOT_FOUND.getStatusCode() && exception.getResponse().getStatus() != Status.METHOD_NOT_ALLOWED.getStatusCode()) {
			log.error("JAX-RS", exception);
		}

		// Map to internal error without exposing the exception
		return toResponse(Response.Status.fromStatusCode(exception.getResponse().getStatus()), "internal", null);
	}

}
