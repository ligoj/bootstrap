/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

/**
 * Simple forward of {@link ValidationJsonException}.
 */
@Provider
public class ValidationJsonExceptionMapper extends AbstractMapper implements ExceptionMapper<ValidationJsonException> {

	@Override
	public Response toResponse(final ValidationJsonException ex) {
		// Set the content type, and JSR-303 error into JSON format.
		return toResponse(Status.BAD_REQUEST, ex);
	}
}
