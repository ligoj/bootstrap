/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

/**
 * Maps a {@link ConstraintViolationException} to a JSR-303 validation error. Status code, and contents are updated.
 */
@Provider
public class Jsr303ExceptionMapper extends AbstractMapper implements ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(final ConstraintViolationException ex) {
		// Set the content type, and JSR-303 error into JSON format.
		return toResponse(Status.BAD_REQUEST, new ValidationJsonException(ex));
	}
}
