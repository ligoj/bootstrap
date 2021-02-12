/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.ligoj.bootstrap.dao.system.AuthorizationRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * Authorization resource.
 */
@Path("/system/security/authorization")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

	@Autowired
	protected AuthorizationRepository repository;

	@Autowired
	private RoleResource resource;

	@Autowired
	protected CacheManager cacheManager;

	@Value("${security.filter.methods:GET,POST,DELETE,PUT}")
	private HttpMethod[] methods;

	/**
	 * Retrieve an authorization from its identifier.
	 *
	 * @param id
	 *            Element's identifier.
	 * @return Found element. May be <code>null</code>.
	 */
	@GET
	@Path("{id:\\d+}")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public SystemAuthorization findById(@PathParam("id") final Integer id) {
		return repository.findOneExpected(id);
	}

	/**
	 * Retrieve all UI authorizations of current user.
	 *
	 * @param context
	 *            security context.
	 * @return all UI authorizations of current user.
	 */
	@GET
	@Path("user/ui")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public List<SystemAuthorization> findAuthorizationsUi(@Context final SecurityContext context) {
		return repository.findAllByLogin(context.getUserPrincipal().getName(), SystemAuthorization.AuthorizationType.UI);
	}

	/**
	 * Retrieve all API authorizations of current user.
	 *
	 * @param context
	 *            Security context.
	 * @return All API authorizations of current user.
	 */
	@GET
	@Path("user/api")
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public List<SystemAuthorization> findAuthorizationsApi(@Context final SecurityContext context) {
		return repository.findAllByLogin(context.getUserPrincipal().getName(), SystemAuthorization.AuthorizationType.API);
	}

	/**
	 * Create a new authorization.
	 *
	 * @param entity
	 *            New object to persist.
	 * @return identifier of created object.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@CacheRemoveAll(cacheName = "authorizations")
	public int create(final AuthorizationEditionVo entity) {
		final var authorization = new SystemAuthorization();
		prepareCreate(authorization, entity);
		return authorization.getId();
	}

	/**
	 * Update element from its identifier.
	 *
	 * @param entity
	 *            Element to update.
	 * @param id
	 *            Element's identifier.
	 */
	@PUT
	@Path("{id:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@CacheRemoveAll(cacheName = "authorizations")
	public void update(@PathParam("id") final int id, final AuthorizationEditionVo entity) {
		prepareUpdate(id, entity);
	}

	/**
	 * Prepare for create a new element.
	 *
	 * @param authorization
	 *            Target new object to persist.
	 * @param entity
	 *            New object to persist.
	 */
	private void prepareCreate(final SystemAuthorization authorization, final AuthorizationEditionVo entity) {
		final var role = resource.findById(entity.getRole());
		authorization.setRole(role);
		authorization.setPattern(entity.getPattern());
		authorization.setType(entity.getType());
		repository.save(authorization);
		Optional.ofNullable(cacheManager.getCache("user-details")).ifPresent(Cache::clear);
	}

	/**
	 * Prepare for update element from its identifier.
	 *
	 * @param entity
	 *            Element to update.
	 * @param id
	 *            Element's identifier.
	 */
	private void prepareUpdate(final int id, final AuthorizationEditionVo entity) {
		final var authorization = repository.findOneExpected(id);
		prepareCreate(authorization, entity);
	}

	/**
	 * Delete Role from its ID
	 *
	 * @param id
	 *            Identifier of element to delete.
	 */
	@DELETE
	@Path("{id:\\d+}")
	@CacheRemoveAll(cacheName = "authorizations")
	public void remove(@PathParam("id") final int id) {
		repository.deleteById(id);
	}

	/**
	 * Return the current authorizations.<br>
	 * Key : authorization type.<br>
	 * Value : another map, where Key is authority (role) name.<br>
	 * Value : another map, where Key is the granted HTTP method. <br>
	 * Value : another map, where Value is the granted HTTP pattern.
	 *
	 * @return The authorizations of each role.
	 */
	@CacheResult(cacheName = "authorizations")
	public Map<AuthorizationType, Map<String, Map<HttpMethod, List<Pattern>>>> getAuthorizations() {
		final Map<AuthorizationType, Map<String, Map<HttpMethod, List<Pattern>>>> authorizationsCache = new EnumMap<>(
				AuthorizationType.class);
		final var authorizations = repository.findAll();
		authorizations.forEach(a -> addAuthorization(newCacheRole(newCacheType(authorizationsCache, a), a), a));
		return authorizationsCache;
	}

	/**
	 * Cache authorization role
	 */
	private Map<HttpMethod, List<Pattern>> newCacheRole(final Map<String, Map<HttpMethod, List<Pattern>>> existingAuthorizations,
			final SystemAuthorization authorization) {
		return existingAuthorizations.computeIfAbsent(authorization.getRole().getName(), r -> new EnumMap<>(HttpMethod.class));
	}

	/**
	 * Cache authorization type
	 */
	private Map<String, Map<HttpMethod, List<Pattern>>> newCacheType(
			final Map<AuthorizationType, Map<String, Map<HttpMethod, List<Pattern>>>> authorizationsCache,
			final SystemAuthorization authorization) {
		return authorizationsCache.computeIfAbsent(authorization.getType(), a -> new HashMap<>());
	}

	/**
	 * Add an authorization to the given cache.
	 */
	private void addAuthorization(final Map<HttpMethod, List<Pattern>> existingAuthorizations, final SystemAuthorization authorization) {
		if (authorization.getMethod() == null) {
			// All methods
			for (final var method : methods) {
				addAuthorization(existingAuthorizations, method, authorization.getPattern());
			}
		} else {
			// Only this specific method
			addAuthorization(existingAuthorizations, authorization.getMethod(), authorization.getPattern());
		}
	}

	/**
	 * Add an pattern/method authorization to the given cache.
	 */
	private void addAuthorization(final Map<HttpMethod, List<Pattern>> existingAuthorizations, final HttpMethod method,
			final String pattern) {
		existingAuthorizations.computeIfAbsent(method, m -> new ArrayList<>()).add(Pattern.compile(pattern));
	}
}
