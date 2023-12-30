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

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Maps a {@link jakarta.validation.ConstraintViolationException} to a JSR-303 validation error. Status code, and contents
 * are updated.
 */
@Provider
public class InvalidFormatExceptionMapper extends AbstractMapper implements ExceptionMapper<InvalidFormatException> {

	@Override
	public Response toResponse(final InvalidFormatException ex) {
		// Set the JSR-303 error into JSON format.
		return toResponse(Status.BAD_REQUEST, new ValidationJsonException(ex));
	}
}
