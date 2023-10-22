/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.ligoj.bootstrap.core.json.PaginationJson;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.dao.system.SystemHookRepository;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheRemoveAll;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * {@link SystemHook} resource.
 */
@Path("/system/hook")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class HookResource {

	@Autowired
	private PaginationJson paginationJson;

	@Autowired
	protected SystemHookRepository repository;

	/**
	 * Ordered columns.
	 */
	private static final Map<String, String> ORDERED_COLUMNS = new HashMap<>();

	static {
		ORDERED_COLUMNS.put("id", "id");
		ORDERED_COLUMNS.put("name", "name");
	}

	/**
	 * Retrieve all elements with pagination
	 *
	 * @param uriInfo  pagination data.
	 * @return all elements with pagination.
	 */
	@GET
	public TableItem<SystemHook> findAll(@Context final UriInfo uriInfo) {
		final var pageRequest = paginationJson.getPageRequest(uriInfo, ORDERED_COLUMNS);
		final var findAll = repository.findAll(pageRequest);

		// apply pagination and prevent lazy initialization issue
		return paginationJson.applyPagination(uriInfo, findAll, Function.identity());
	}

	/**
	 * Create a delegate.
	 *
	 * @param vo the object to create.
	 * @return the entity's identifier.
	 */
	@POST
	@PUT
	@CacheRemoveAll(cacheName = "hooks")
	public int create(final SystemHook vo) {
		return repository.saveAndFlush(vo).getId();
	}

	/**
	 * Delete entity that must exist.
	 *
	 * @param id the entity identifier.
	 */
	@DELETE
	@Path("{id:\\d+}")
	@CacheRemoveAll(cacheName = "hooks")
	public void delete(@PathParam("id") final int id) {
		repository.deleteAllExpected(List.of(id));
	}
}
