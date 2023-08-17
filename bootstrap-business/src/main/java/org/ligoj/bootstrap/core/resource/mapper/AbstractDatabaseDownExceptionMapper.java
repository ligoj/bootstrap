/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.ligoj.bootstrap.core.resource.AbstractMapper;

/**
 * Handles database issue to a JSON string.
 */
public abstract class AbstractDatabaseDownExceptionMapper extends AbstractMapper {

	protected Response toResponse() {
		return toResponse(Status.SERVICE_UNAVAILABLE, "database-down", null);
	}

}
