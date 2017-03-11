package org.ligoj.bootstrap.core.resource.mapper;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Handles business exception (explicitly managed) {@link EntityNotFoundException} to a JSON string, and a 404 status code error.
 */
@Provider
public class EntityNotFoundExceptionMapper extends AbstractEntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

	@Override
	public Response toResponse(final EntityNotFoundException exception) {
		return toResponseNotFound(exception);
	}

}
