/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.validation;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.ligoj.bootstrap.core.validation.Wine;
import org.springframework.stereotype.Service;

/**
 * Test Business Layer
 */
@Path("/test/validation")
@Produces(MediaType.APPLICATION_JSON)
@Service
public class ValidationTestResource {
	/**
	 * Simple POST validation.
	 * 
	 * @param entity The entity to pass to JAX service.
	 * @return The persisted identifier.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public int create(final Wine entity) {
		return entity.getId();
	}

	/**
	 * Simple PUT validation.
	 * 
	 * @param entity The entity to pass to JAX service.
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(final Wine entity) {
		// Nothing to do;
	}

}
