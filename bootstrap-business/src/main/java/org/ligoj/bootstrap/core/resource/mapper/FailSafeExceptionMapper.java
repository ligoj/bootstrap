/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import javax.naming.CommunicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ligoj.bootstrap.core.resource.AbstractMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles uncaught {@link Throwable} errors exception to a JSON string. This is a fail-safe security guard against
 * stack trace exposition for REST services.
 */
@Provider
@Slf4j
public class FailSafeExceptionMapper extends AbstractMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(final Throwable exception) {

		/*
		 * Check LDAP communication issue. As this exception is wrapped by a runtime exception brought bySpring-LDAP
		 * (optional dependency), there is no way a create a specific Mapper.
		 */
		if (exception.getCause() instanceof CommunicationException) {
			log.error("LDAP exception", exception);
			return toResponse(Status.SERVICE_UNAVAILABLE, "ldap-down", exception.getCause());
		}

		// Really not managed exception
		log.error("Non managed error", exception);
		
		// Don't expose the associated exception or message since we ignore the content
		return toResponse(Status.INTERNAL_SERVER_ERROR, "internal", null);
	}

}
