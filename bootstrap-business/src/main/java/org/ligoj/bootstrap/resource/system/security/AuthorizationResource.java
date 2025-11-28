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
import java.util.stream.Stream;

import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import org.ligoj.bootstrap.dao.system.AuthorizationRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
	private String[] methods;

	/**
	 * Retrieve an authorization from its identifier.
	 *
	 * @param id Element's identifier.
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
	 * @param context security context.
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
	 * @param context Security context.
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
	 * @param entity New object to persist.
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
	 * @param entity Element to update.
	 * @param id     Element's identifier.
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
	 * @param authorization Target new object to persist.
	 * @param entity        New object to persist.
	 */
	private void prepareCreate(final SystemAuthorization authorization, final AuthorizationEditionVo entity) {
		final var role = resource.findById(entity.getRole());
		authorization.setRole(role);
		authorization.setPattern(entity.getPattern());
		authorization.setType(entity.getType());
		repository.saveAndFlush(authorization);
		Optional.ofNullable(cacheManager.getCache("user-details")).ifPresent(Cache::clear);
	}

	/**
	 * Prepare for update element from its identifier.
	 *
	 * @param entity Element to update.
	 * @param id     Element's identifier.
	 */
	private void prepareUpdate(final int id, final AuthorizationEditionVo entity) {
		final var authorization = repository.findOneExpected(id);
		prepareCreate(authorization, entity);
	}

	/**
	 * Delete Role from its ID
	 *
	 * @param id Identifier of element to delete.
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
	public Map<AuthorizationType, Map<String, Map<String, List<Pattern>>>> getAuthorizations() {
		final Map<AuthorizationType, Map<String, Map<String, List<Pattern>>>> cache = new EnumMap<>(
				AuthorizationType.class);
		repository.findAll().forEach(a -> addAuthorization(newCacheRole(newCacheType(cache, a), a), a));
		return cache;
	}

	/**
	 * Cache authorization role
	 */
	private Map<String, List<Pattern>> newCacheRole(final Map<String, Map<String, List<Pattern>>> existingAuthorizations,
			final SystemAuthorization authorization) {
		return existingAuthorizations.computeIfAbsent(authorization.getRole().getName(), r -> new HashMap<>());
	}

	/**
	 * Cache authorization type
	 */
	private Map<String, Map<String, List<Pattern>>> newCacheType(
			final Map<AuthorizationType, Map<String, Map<String, List<Pattern>>>> authorizationsCache,
			final SystemAuthorization authorization) {
		return authorizationsCache.computeIfAbsent(authorization.getType(), a -> new HashMap<>());
	}

	/**
	 * Add an authorization to the given cache.
	 */
	private void addAuthorization(final Map<String, List<Pattern>> existingAuthorizations, final SystemAuthorization authorization) {
		if (authorization.getMethod() == null) {
			// All methods
			Stream.of(methods).forEach(m -> addAuthorization(existingAuthorizations, m, authorization.getPattern()));
		} else {
			// Only this specific method
			addAuthorization(existingAuthorizations, authorization.getMethod(), authorization.getPattern());
		}
	}

	/**
	 * Add a pattern/method authorization to the given cache.
	 */
	private void addAuthorization(final Map<String, List<Pattern>> existingAuthorizations, final String method,
			final String pattern) {
		var patterns = existingAuthorizations.computeIfAbsent(method, m -> new ArrayList<>());
		// Add the pattern if it is not yet in the list as compiled Pattern

		if (".*".equals(pattern)) {
			existingAuthorizations.put(method, List.of(Pattern.compile(pattern)));
		} else if (patterns.stream().noneMatch(p -> p.pattern().equals(".*") || p.pattern().equals(pattern))) {
			patterns.add(Pattern.compile(pattern));
		}
	}
}
