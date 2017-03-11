package org.ligoj.bootstrap.core.resource.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ligoj.bootstrap.core.resource.AbstractMapper;

/**
 * Handles database issue to a JSON string.
 */
public abstract class AbstractDatabaseDownExceptionMapper extends AbstractMapper {

	protected Response toResponse() {
		return toResponse(Status.SERVICE_UNAVAILABLE, "database-down", null);
	}

}
