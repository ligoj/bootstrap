/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;

/**
 * Handles business exception (explicitly managed) {@link JpaObjectRetrievalFailureException} (wrapping
 * EntityNotFoundException) to a JSON string, and a 404 status code error.
 */
@Provider
public class JpaObjectRetrievalFailureExceptionMapper extends AbstractEntityNotFoundExceptionMapper implements ExceptionMapper<JpaObjectRetrievalFailureException> {

	@Override
	public Response toResponse(final JpaObjectRetrievalFailureException exception) {
		return toResponseNotFound(exception.getCause());
	}

}
