/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.validation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ligoj.bootstrap.core.validation.Wine;
import org.springframework.stereotype.Service;

/**
 * Wine Business Layer
 */
@Path("/test/validation")
@Produces(MediaType.APPLICATION_JSON)
@Service
public class ValidationTestResource {
	/**
	 * Simple POST validation.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public int create(final Wine wine) {
		return wine.getId();
	}

	/**
	 * Update wine from its ID.
	 * 
	 * @param wine
	 *            data that will be updated
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(final Wine wine) {
		// Nothing to do;
	}

}
