/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.resource.BusinessException;

/**
 * Handles business exception (explicitly managed) {@link BusinessException} to a JSON string.
 */
@Provider
public class BusinessExceptionMapper extends AbstractMapper implements ExceptionMapper<BusinessException> {

	@Override
	public Response toResponse(final BusinessException exception) {
		return toResponse(Status.BAD_REQUEST, "business", exception);
	}

}
