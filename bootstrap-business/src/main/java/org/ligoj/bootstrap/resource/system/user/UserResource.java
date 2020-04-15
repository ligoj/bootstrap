/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemove;
import javax.persistence.criteria.JoinType;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.ligoj.bootstrap.core.dao.PaginationDao;
import org.ligoj.bootstrap.core.json.PaginationJson;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.dao.system.SystemRoleAssignmentRepository;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.security.SystemRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Manage {@link SystemUser}
 */
@Service
@Path("/system/user")
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	@Autowired
	private PaginationDao pagination;

	@Autowired
	private PaginationJson paginationJson;

	@Autowired
	private SystemUserRepository repository;

	@Autowired
	private SystemRoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	protected CacheManager cacheManager;

	/**
	 * Ordered columns.
	 */
	private static final Map<String, String> ORDERED_COLUMNS = new HashMap<>();

	static {
		ORDERED_COLUMNS.put("login", "login");
		ORDERED_COLUMNS.put("role", "roles.role.name");
	}

	/**
	 * Fetched associations.
	 */
	private static final Map<String, JoinType> FETCHED_ASSOCS = new HashMap<>();

	static {
		FETCHED_ASSOCS.put("roles", JoinType.LEFT);
	}

	private static final ToBusinessConverterRole TO_BUSINESS_ROLES = new ToBusinessConverterRole();

	private static final ToBusinessConverter TO_BUSINESS = new ToBusinessConverter();

	/**
	 * {@link SystemUser} JPA to business object transformer.
	 */
	private static class ToBusinessConverter implements Function<SystemUser, String> {

		@Override
		public String apply(final SystemUser user) {
			return user.getLogin();
		}
	}

	/**
	 * {@link SystemUser} JPA to business object transformer.
	 */
	private static class ToBusinessConverterRole implements Function<SystemUser, SystemUserVo> {

		@Override
		public SystemUserVo apply(final SystemUser user) {
			final var userVo = new SystemUserVo();
			userVo.setLogin(user.getLogin());
			userVo.setRoles(new ArrayList<>());
			final var roles = user.getRoles();
			for (final var role : roles) {
				final var systemRoleVo = new SystemRoleVo();
				systemRoleVo.setId(role.getRole().getId());
				systemRoleVo.setName(role.getRole().getName());
				userVo.getRoles().add(systemRoleVo);
			}
			return userVo;
		}
	}

	/**
	 * Retrieve a User by its identifier.
	 * 
	 * @param login
	 *            user identifier.
	 * @return User.
	 */
	@GET
	@Path("{login}")
	public SystemUser findById(@PathParam("login") final String login) {
		return repository.findOneExpected(login);
	}

	/**
	 * Return all users.
	 * 
	 * @param uriInfo
	 *            Query context.
	 * @return all users.
	 */
	@GET
	public TableItem<String> findAll(@Context final UriInfo uriInfo) {
		return paginationJson.applyPagination(uriInfo, repository.findAll(paginationJson.getPageRequest(uriInfo, ORDERED_COLUMNS)), TO_BUSINESS);
	}

	/**
	 * Return all users with roles.
	 * 
	 * @param uriInfo
	 *            Query context.
	 * @return all users.
	 */
	@GET
	@Path("roles")
	public TableItem<SystemUserVo> findAllWithRoles(@Context final UriInfo uriInfo) {
		final var findAll = pagination.findAll(SystemUser.class, uriInfo, ORDERED_COLUMNS, null, FETCHED_ASSOCS);
		// apply pagination
		return paginationJson.applyPagination(uriInfo, findAll, TO_BUSINESS_ROLES);
	}

	/**
	 * Create the user and return the corresponding identifier.
	 * 
	 * @param userVo
	 *            the user to create.
	 */
	@POST
	public void create(final SystemUserEditionVo userVo) {
        var user = new SystemUser();
		user.setLogin(userVo.getLogin());
		// create the user
		user = repository.save(user);
		// update role assignment
		createRoleAssignment(userVo.getRoles(), user);
	}

	/**
	 * Update the user.
	 * 
	 * @param userVo
	 *            the user to update.
	 */
	@PUT
	public void update(final SystemUserEditionVo userVo) {
		final var user = repository.findOneExpected(userVo.getLogin());
		final var roleToDelete = new ArrayList<SystemRoleAssignment>();
		// remove roles deleted by the user
		for (final var role : user.getRoles()) {
			if (userVo.getRoles().contains(role.getRole().getId())) {
				userVo.getRoles().remove(role.getRole().getId());
			} else {
				roleToDelete.add(role);
			}
		}
		roleAssignmentRepository.deleteAll(roleToDelete);
		user.getRoles().removeAll(roleToDelete);
		// create new roles assignment
		createRoleAssignment(userVo.getRoles(), user);
	}

	/**
	 * Create role assignment.
	 * 
	 * @param roleIds
	 *            The role identifiers.
	 * @param user
	 *            The user to assign.
	 */
	private void createRoleAssignment(final List<Integer> roleIds, final SystemUser user) {
		for (final var roleId : roleIds) {
			final var roleAssignment = new SystemRoleAssignment();
			final var role = new SystemRole();
			role.setId(roleId);
			roleAssignment.setRole(role);
			roleAssignment.setUser(user);
			roleAssignmentRepository.save(roleAssignment);
		}
		cacheManager.getCache("user-details").evict(user.getLogin());
	}

	/**
	 * Delete an {@link SystemUser}
	 * 
	 * @param login
	 *            the user login.
	 */
	@DELETE
	@Path("{login}")
	@CacheRemove(cacheName = "user-details")
	public void delete(@PathParam("login") @CacheKey final String login) {
		repository.deleteById(login);
	}
}
