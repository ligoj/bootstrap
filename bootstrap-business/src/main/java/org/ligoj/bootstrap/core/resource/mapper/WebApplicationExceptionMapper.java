package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
		return toResponse(Response.Status.fromStatusCode(exception.getResponse().getStatus()), "internal", exception);
	}

}
