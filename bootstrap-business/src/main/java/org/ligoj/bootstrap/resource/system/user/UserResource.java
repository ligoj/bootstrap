/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.NamedBean;
import org.ligoj.bootstrap.core.dao.PaginationDao;
import org.ligoj.bootstrap.core.json.PaginationJson;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.dao.system.SystemRoleAssignmentRepository;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;
import org.ligoj.bootstrap.resource.system.security.SystemRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemove;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Function;

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
	private ApiTokenResource apiTokenResource;

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
	private static final Map<String, JoinType> FETCHED_ASSOCIATIONS = new HashMap<>();

	static {
		FETCHED_ASSOCIATIONS.put("roles", JoinType.LEFT);
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
			final var uniqueRoles = new HashSet<String>();
			for (final var role : roles) {
				if (uniqueRoles.add(role.getRole().getName())) {
					final var systemRoleVo = new SystemRoleVo();
					systemRoleVo.setId(role.getRole().getId());
					systemRoleVo.setName(role.getRole().getName());
					userVo.getRoles().add(systemRoleVo);
				}
			}
			return userVo;
		}
	}

	/**
	 * Retrieve a User by its identifier.
	 *
	 * @param login user identifier.
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
	 * @param uriInfo Query context.
	 * @return all users.
	 */
	@GET
	public TableItem<String> findAll(@Context final UriInfo uriInfo) {
		return paginationJson.applyPagination(uriInfo, repository.findAll(paginationJson.getPageRequest(uriInfo, ORDERED_COLUMNS)), TO_BUSINESS);
	}

	/**
	 * Return all users with roles.
	 *
	 * @param uriInfo Query context.
	 * @return all users.
	 */
	@GET
	@Path("roles")
	public TableItem<SystemUserVo> findAllWithRoles(@Context final UriInfo uriInfo) {
		final var findAll = pagination.findAll(SystemUser.class, uriInfo, ORDERED_COLUMNS, null, FETCHED_ASSOCIATIONS);
		// apply pagination
		return paginationJson.applyPagination(uriInfo, findAll, TO_BUSINESS_ROLES);
	}

	/**
	 * Create the user as needed and return the corresponding identifier.
	 *
	 * @param userVo the user to create.
	 * @return the generated token if requested.
	 * @throws GeneralSecurityException When there is a security issue.
	 */
	@POST
	public NamedBean<String> create(final SystemUserEditionVo userVo) throws GeneralSecurityException {
		final var targetUser = userVo.getLogin();
		var user = Objects.requireNonNullElseGet(repository.findOne(targetUser), () -> {
			final var newUser = new SystemUser();
			newUser.setLogin(targetUser);
			newUser.setRoles(new HashSet<>());
			return newUser;
		});

		// create the user
		user = repository.save(user);

		// update role assignment
		createRoleAssignment(userVo.getRoles(), user);

		if (StringUtils.isNotBlank(userVo.getApiToken()) && !apiTokenResource.hasToken(targetUser, userVo.getApiToken())) {
			return apiTokenResource.create(targetUser, userVo.getApiToken());
		}
		return null;
	}

	/**
	 * Update the user.
	 *
	 * @param userVo the user to update.
	 */
	@PUT
	public void update(final SystemUserEditionVo userVo) {
		final var user = repository.findOneExpected(userVo.getLogin());
		createRoleAssignment(userVo.getRoles(), user);
	}

	/**
	 * Create role assignment.
	 *
	 * @param targetRoles The desired role identifiers.
	 * @param user        The user to assign.
	 */
	private void createRoleAssignment(final Set<Integer> targetRoles, final SystemUser user) {
		// Delete previous assignment roles
		final var deletedRoles = new HashSet<>(user.getRoles().stream().map(r -> r.getRole().getId()).toList());
		deletedRoles.removeAll(targetRoles);

		// Create the new assignments
		final var newRoles = new HashSet<>(targetRoles);
		user.getRoles().stream().map(r -> r.getRole().getId()).forEach(newRoles::remove);

		// Apply the changes
		deletedRoles.forEach(r -> roleAssignmentRepository.deleteAllBy("user.id", user.getLogin(), new String[]{"role.id"}, r));
		newRoles.forEach(r -> {
			final var roleAssignment = new SystemRoleAssignment();
			final var role = new SystemRole();
			role.setId(r);
			roleAssignment.setRole(role);
			roleAssignment.setUser(user);
			roleAssignmentRepository.save(roleAssignment);
		});

		cacheManager.getCache("user-details").evict(user.getLogin());
	}

	/**
	 * Delete an {@link SystemUser}
	 *
	 * @param login the user login.
	 */
	@DELETE
	@Path("{login}")
	@CacheRemove(cacheName = "user-details")
	public void delete(@PathParam("login") @CacheKey final String login) {
		apiTokenResource.removeAll(login);
		roleAssignmentRepository.deleteAllBy("user.id", login);
		repository.deleteById(login);
	}
}
