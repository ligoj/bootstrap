package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
