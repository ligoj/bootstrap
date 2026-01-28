/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.json.PaginationJson;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.dao.system.SystemHookRepository;
import org.ligoj.bootstrap.model.system.HookMatch;
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
import java.util.regex.Pattern;

/**
 * {@link SystemHook} resource.
 */
@Path("/system/hook")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class HookResource {

	@Autowired
	private ObjectMapperTrim objectMapper;

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
	public SystemHook findByName(@PathParam("name") String name) {
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
	 * Return true when given command is disallowed according to 'ligoj.hook.path' values.
	 *
	 * @param configurationResource The configuration resource to retrieve the value of 'ligoj.hook.path'.
	 * @param command               The command to execute.
	 * @return true when given command is disallowed according to 'ligoj.hook.path' values.
	 */
	static boolean isForbiddenCommand(final ConfigurationResource configurationResource, final String command) {
		return Arrays.stream(configurationResource.get("ligoj.hook.path", "^$").split(",")).noneMatch(command::matches);
	}

	/**
	 * Create a hook.
	 *
	 * @param vo the object to create.
	 * @return the entity's identifier.
	 * @throws JsonProcessingException When <code>math</code> parameter is not a valid JSON.
	 */
	@POST
	@CacheRemoveAll(cacheName = "hooks")
	public int create(final SystemHook vo) throws JsonProcessingException {
		check(vo);
		return repository.saveAndFlush(vo).getId();
	}

	private void check(final SystemHook vo) throws JsonProcessingException {
		if (isForbiddenCommand(configurationResource, vo.getCommand())) {
			throw new ForbiddenException("Hook command is not within one of allowed ${ligoj.hook.path} value");
		}

		// Validate the path
		Pattern.compile(objectMapper.readValue(vo.getMatch(), HookMatch.class).getPath());
	}

	/**
	 * Update a hook by identifier or by name.
	 *
	 * @param vo the object to update.
	 * @return the entity's identifier.
	 * @throws JsonProcessingException When <code>math</code> parameter is not a valid JSON.
	 */
	@PUT
	@CacheRemoveAll(cacheName = "hooks")
	public int update(final SystemHook vo) throws JsonProcessingException {
		check(vo);
		SystemHook entity;
		if (vo.getId() == null) {
			entity = repository.findByNameExpected(vo.getName());
		} else {
			entity = repository.findOneExpected(vo.getId());
		}
		entity.setCommand(vo.getCommand());
		entity.setWorkingDirectory(vo.getWorkingDirectory());
		entity.setMatch(vo.getMatch());
		entity.setInject(vo.getInject());
		entity.setName(vo.getName());
		entity.setTimeout(vo.getTimeout());
		entity.setDelay(vo.getDelay());
		return repository.saveAndFlush(entity).getId();
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
