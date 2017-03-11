package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.resource.BusinessException;

/**
 * Handles business exception (explicitly managed) {@link BusinessException} to a JSON string.
 */
@Provider
public class BusinessExceptionMapper extends AbstractMapper implements ExceptionMapper<BusinessException> {

	@Override
	public Response toResponse(final BusinessException exception) {
		return toResponse(Status.INTERNAL_SERVER_ERROR, "business", exception);
	}

}
