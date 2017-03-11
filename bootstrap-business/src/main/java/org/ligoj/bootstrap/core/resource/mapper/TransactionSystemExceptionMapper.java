package org.ligoj.bootstrap.core.resource.mapper;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.transaction.TransactionSystemException;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps a {@link TransactionSystemException} wrapping {@link ConstraintViolationException} to a JSR-303 validation error. Status code, and contents are updated.
 */
@Provider
@Slf4j
public class TransactionSystemExceptionMapper extends AbstractMapper implements ExceptionMapper<TransactionSystemException> {

	@Override
	public Response toResponse(final TransactionSystemException ex) {
		if (ExceptionUtils.getRootCause(ex) instanceof ConstraintViolationException) {
			// Set the content type, and JSR-303 error into JSON format.
			return toResponse(Status.BAD_REQUEST, new ValidationJsonException((ConstraintViolationException) ExceptionUtils.getRootCause(ex)));
		}
		
		// Not yet managed exception
		log.error("Technical exception", ex);
		return toResponse(Status.INTERNAL_SERVER_ERROR, "technical", ex);
	}
}
