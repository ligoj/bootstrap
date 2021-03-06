/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Map any server error to a JSON string.
 */
public abstract class AbstractMapper {

	@Autowired
	protected JacksonJsonProvider jacksonJsonProvider;

	/**
	 * Writes a JSON string corresponding to the given error to a {@link Response}.
	 * 
	 * @param status
	 *            the error status.
	 * @param errorKey
	 *            the error key that will be used for localization.
	 * @param ex
	 *            the technical exception.
	 * @return the response containing JSON entity.
	 */
	protected Response toResponse(final Response.StatusType status, final String errorKey, final Throwable ex) {
		final var serverError = new ServerError();
		serverError.setCode(errorKey);
		if (ex != null) {
			serverError.setThrowable(ex);
		}
		if (ex instanceof AbstractParameteredException && ((AbstractParameteredException) ex).getParameters().length > 0) {
			serverError.setParameters(((AbstractParameteredException) ex).getParameters());
		}
		return toResponse(status, serverError);
	}

	/**
	 * Writes a JSON string corresponding to the given object a {@link Response}.
	 * 
	 * @param status
	 *            the error status.
	 * @param object
	 *            the error object.
	 * @return the response containing JSON entity.
	 */
	protected Response toResponse(final Response.StatusType status, final Object object) {
		return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(toEntity(object)).build();
	}

	/**
	 * Return a JSON string corresponding to the given object.
	 * 
	 * @param object
	 *            the error object.
	 * @return the JSON entity.
	 */
	protected Object toEntity(final Object object) {
		try {
			return jacksonJsonProvider.locateMapper(object.getClass(), MediaType.APPLICATION_JSON_TYPE).writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			// Ignore this error at UI level but trace it
			throw new TechnicalException("Unable to build a JSON string from a server error", e);
		}
	}
}
