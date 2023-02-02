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

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

/**
 * Maps a {@link MismatchedInputException} to a JSR-303 validation error. Status code, and contents
 * are updated.
 */
@Provider
public class MismatchedInputExceptionMapper extends AbstractMapper implements ExceptionMapper<MismatchedInputException> {

	@Override
	public Response toResponse(final MismatchedInputException ex) {
		// Set the JSR-303 error into JSON format.
		return toResponse(Status.BAD_REQUEST, new ValidationJsonException(ex));
	}
}
