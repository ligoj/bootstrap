/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.springframework.dao.DataAccessResourceFailureException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles database (no transaction) issue to a JSON string.
 */
@Provider
@Slf4j
public class DataAccessResourceFailureExceptionMapper extends AbstractDatabaseDownExceptionMapper
		implements ExceptionMapper<DataAccessResourceFailureException> {

	@Override
	public Response toResponse(final DataAccessResourceFailureException exception) {
		log.error("Connection exception", exception);
		return super.toResponse();
	}

}
