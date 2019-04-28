/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.cache.annotation.CacheRemoveAll;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.dao.system.AuthorizationRepository;
import org.ligoj.bootstrap.dao.system.SystemRoleAssignmentRepository;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Role management.
 */
@Path("/system/security/role")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class RoleResource {

	/**
	 * Role identifier JQL path.
	 */
	private static final String ROLE_ID = "role.id";

	@Autowired
	private SystemRoleRepository repository;

	@Autowired
	private SystemRoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private AuthorizationRepository authorizationRepository;

	/**
	 * Retrieve an element from its identifier.
	 * 
	 * @param id
	 *            Element's identifier.
	 * @return Found element. May be <code>null</code>.
	 */
	@GET
	@Path("{id:\\d+}")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public SystemRole findById(@PathParam("id") final Integer id) {
		return repository.findOneExpected(id);
	}

	/**
	 * Retrieve all elements without pagination.
	 * 
	 * @return all elements without pagination.
	 */
	@GET
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public TableItem<SystemRole> findAll() {
		final var result = new TableItem<SystemRole>();
		result.setData(repository.findAll());
		return result;
	}

	/**
	 * Retrieve all elements without pagination and includes authorizations..
	 * 
	 * @return all elements without pagination.
	 */
	@GET
	@Path("withAuth")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public TableItem<SystemRoleVo> findAllFetchAuth() {
		final var result = new TableItem<SystemRoleVo>();
		// get all roles
		final Map<Integer, SystemRoleVo> results = new TreeMap<>();
		fetchRoles(results);

		// fetch authorizations
		fetchAuthorizations(results);

		// apply pagination
		result.setData(new ArrayList<>(results.values()));
		result.setRecordsTotal(results.size());
		result.setRecordsTotal(results.size());
		return result;
	}

	/**
	 * Fetch roles and add them to result parameter.
	 */
	private void fetchRoles(final Map<Integer, SystemRoleVo> results) {
		final var roles = repository.findAll();
		for (final var role : roles) {
			final var roleVo = new SystemRoleVo();
			roleVo.setId(role.getId());
			roleVo.setName(role.getName());
			results.put(role.getId(), roleVo);
		}
	}

	/**
	 * Fetch authorizations and add them to result parameter.
	 */
	private void fetchAuthorizations(final Map<Integer, SystemRoleVo> results) {
		final var auths = authorizationRepository.findAll();
		for (final var auth : auths) {
			final var authVo = new AuthorizationEditionVo();
			results.get(auth.getRole().getId()).getAuthorizations().add(authVo);
			authVo.setId(auth.getId());
			authVo.setPattern(auth.getPattern());
			authVo.setType(auth.getType());
		}
	}

	/**
	 * Create a new element.
	 * 
	 * @param roleVo
	 *            The role to create.
	 * @return identifier of created object.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@CacheRemoveAll(cacheName = "authorizations")
	public int create(final SystemRoleVo roleVo) {
		final var role = new SystemRole();
		role.setName(roleVo.getName());
		// create role
		final var roleId = repository.saveAndFlush(role).getId();

		// create authorizations
		for (final var authVo : roleVo.getAuthorizations()) {
			final var auth = new SystemAuthorization();
			auth.setRole(role);
			auth.setPattern(authVo.getPattern());
			auth.setType(authVo.getType());
			authorizationRepository.save(auth);
		}
		return roleId;
	}

	/**
	 * Update Role from its ID.
	 * 
	 * @param roleVo
	 *            Role data that will be updated.
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@CacheRemoveAll(cacheName = "authorizations")
	public void update(final SystemRoleVo roleVo) {
		final var role = findById(roleVo.getId());
		role.setName(roleVo.getName());
		// delete authorizations
		for (final var auth : authorizationRepository.findAllBy(ROLE_ID, role.getId())) {
			if (roleVo.getAuthorizations().stream().noneMatch(authVo -> auth.getId().equals(authVo.getId()))) {
				authorizationRepository.delete(auth);
			}
		}
		// create new ones
		for (final var authVo : roleVo.getAuthorizations()) {
			if (authVo.getId() == null) {
				final var auth = new SystemAuthorization();
				auth.setRole(role);
				auth.setPattern(authVo.getPattern());
				auth.setType(authVo.getType());
				authorizationRepository.save(auth);
			}
		}
		repository.save(role);
	}

	/**
	 * Delete Role from its identifier
	 * 
	 * @param id
	 *            Identifier of the role to delete.
	 */
	@DELETE
	@Path("{id:\\d+}")
	@CacheRemoveAll(cacheName = "authorizations")
	public void remove(@PathParam("id") final int id) {
		authorizationRepository.deleteAllBy(ROLE_ID, id);
		roleAssignmentRepository.deleteAllBy(ROLE_ID, id);
		repository.deleteById(id);
	}
}
