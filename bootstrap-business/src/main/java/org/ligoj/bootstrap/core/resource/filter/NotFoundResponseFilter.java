/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.resource.OnNullReturn404;
import org.ligoj.bootstrap.core.resource.ServerError;

/**
 * A response filter able to transform Null results to 404 HTTP response when
 */
@Provider
public class NotFoundResponseFilter extends AbstractMapper implements ContainerResponseFilter {

	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
		if (responseContext.getStatus() == Status.NO_CONTENT.getStatusCode()) {
			// No entity returned
			for (final var annotation : responseContext.getEntityAnnotations()) {
				if (annotation.annotationType() == OnNullReturn404.class) {
					// Explicit management of null result -> return a 404 status code
					replaceResponse(requestContext, responseContext);
					return;
				}
			}
		}
	}

	/**
	 * Set the entity response to a 404 JSON entity.
	 */
	private void replaceResponse(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
		final var serverError = new ServerError();
		if (requestContext.getUriInfo().getPathParameters().size() == 1) {
			// Single parameter ---> entity identifier or whatever identifying a data
			serverError.setCode("entity");
			serverError.setMessage(requestContext.getUriInfo().getPathParameters().values().iterator().next().get(0));
		} else {
			serverError.setCode("data");
		}
		responseContext.setStatus(Status.NOT_FOUND.getStatusCode());
		responseContext.setEntity(toEntity(serverError), responseContext.getEntityAnnotations(), MediaType.APPLICATION_JSON_TYPE);

	}

}
