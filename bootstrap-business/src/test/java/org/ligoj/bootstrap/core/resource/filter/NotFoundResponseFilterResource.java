/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.ligoj.bootstrap.core.resource.OnNullReturn404;

import lombok.extern.slf4j.Slf4j;

/**
 * ContainerResponse resource test.
 */
@Path("/filter")
@Slf4j
public class NotFoundResponseFilterResource {

	/**
	 * Null result without any response manipulation.
	 * @return <code>null</code>.
	 */
	@GET
	@Path("null")
	public String returnNull() {
		return null;
	}

	/**
	 * Null result, should gives a 404.
	 * @return <code>null</code>.
	 */
	@GET
	@Path("null404")
	@OnNullReturn404
	public String returnNull404() {
		return returnNull();
	}

	/**
	 * Null result, should gives a 404 with a specific identifier.
	 * @param id Identifier
	 * @return non null.
	 */
	@GET
	@Path("null404/{id}")
	@OnNullReturn404
	public String returnNull404(@PathParam("id") final int id) {
		log.info("null404-id : " + id);
		return returnNull();
	}

	/**
	 * Not null result, should gives a 200.
	 * @return non <code>null</code>.
	 */
	@GET
	@Path("not-null")
	public String returnNotNull() {
		return "string";
	}
}
