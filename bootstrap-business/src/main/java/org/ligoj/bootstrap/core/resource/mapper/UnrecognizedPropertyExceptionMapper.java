package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

/**
 * Reduce {@link UnrecognizedPropertyException} technical information exposition.
 */
@Provider
public class UnrecognizedPropertyExceptionMapper extends AbstractMapper implements ExceptionMapper<UnrecognizedPropertyException> {

	@Override
	public Response toResponse(final UnrecognizedPropertyException ex) {
		// Set the JSR-303 error into JSON format.
		return toResponse(Status.BAD_REQUEST, new ValidationJsonException(ex));
	}
}
