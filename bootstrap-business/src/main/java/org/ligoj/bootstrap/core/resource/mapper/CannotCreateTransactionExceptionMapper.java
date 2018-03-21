/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.transaction.CannotCreateTransactionException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles transaction creation issue to a JSON string.
 */
@Provider
@Slf4j
public class CannotCreateTransactionExceptionMapper extends AbstractDatabaseDownExceptionMapper implements ExceptionMapper<CannotCreateTransactionException> {

	@Override
	public Response toResponse(final CannotCreateTransactionException exception) {
		log.error("Transaction exception", exception);
		return super.toResponse();
	}

}
