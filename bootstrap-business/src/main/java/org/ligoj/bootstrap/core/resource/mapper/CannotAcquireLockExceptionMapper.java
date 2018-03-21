/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.springframework.dao.CannotAcquireLockException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles transaction issue to acquire a lock on a row.
 */
@Provider
@Slf4j
public class CannotAcquireLockExceptionMapper extends AbstractMapper implements ExceptionMapper<CannotAcquireLockException> {

	@Override
	public Response toResponse(final CannotAcquireLockException exception) {
		log.error("CannotAcquireLockException exception", exception);
		return toResponse(Status.CONFLICT, "database-lock", null);
	}
}
