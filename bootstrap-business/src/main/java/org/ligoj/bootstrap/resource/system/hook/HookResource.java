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
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheRemoveAll;
import java.util.Arrays;
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

	@Autowired
	protected ConfigurationResource configurationResource;

	/**
	 * Ordered columns.
	 */
	private static final Map<String, String> ORDERED_COLUMNS = new HashMap<>();

	static {
		ORDERED_COLUMNS.put("id", "id");
		ORDERED_COLUMNS.put("name", "name");
	}

	/**
	 * Return a hook from ots identifier.
	 *
	 * @param id Hook identifier.
	 * @return a hook from its identifier.
	 */
	@GET
	@Path("{id:\\d}")
	public SystemHook findById(@PathParam("id") int id) {
		return repository.findOneExpected(id);
	}

	/**
	 * Return a hook from its name.
	 *
	 * @param name Hook name.
	 * @return a hook from its name.
	 */
	@GET
	@Path("name/{name}")
	public SystemHook findByName(@PathParam("id") String name) {
		return repository.findByNameExpected(name);
	}

	/**
	 * Retrieve all elements with pagination
	 *
	 * @param uriInfo pagination data.
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
	 * Return true when given command is allowed according to 'ligoj.hook.path' values.
	 *
	 * @param configurationResource The configuration resource to retrieve the value of 'ligoj.hook.path'.
	 * @param command               The command to execute.
	 * @return true when given command is allowed according to 'ligoj.hook.path' values.
	 */
	static boolean isAllowedCommand(final ConfigurationResource configurationResource, final String command) {
		return Arrays.stream(configurationResource.get("ligoj.hook.path", "^$").split(",")).anyMatch(command::matches);
	}

	/**
	 * Create a hook.
	 *
	 * @param vo the object to create.
	 * @return the entity's identifier.
	 */
	@POST
	@PUT
	@CacheRemoveAll(cacheName = "hooks")
	public int create(final SystemHook vo) {
		if (!isAllowedCommand(configurationResource, vo.getCommand())) {
			throw new ForbiddenException("Hook command is not within one of allowed ${ligoj.hook.path} value");
		}

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
