/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import lombok.extern.slf4j.Slf4j;
import org.ligoj.bootstrap.dao.system.AuthorizationRepository;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.session.ISessionSettingsProvider;
import org.ligoj.bootstrap.resource.system.cache.LocalCacheSnapshot;
import org.ligoj.bootstrap.resource.system.security.AuthorizationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User details service backed in the database. All authenticated users get the role {@link SystemRole#DEFAULT_ROLE}
 */
@Component
@Slf4j
public class RbacUserDetailsService implements UserDetailsService {

	/**
	 * User repository.
	 */
	@Autowired
	private SystemUserRepository userRepository;

	@Autowired
	private AuthorizationRepository authorizationRepository;

	/**
	 * Local snapshot of the administrator roles, derived from the authorizations and invalidated with them.
	 */
	private final LocalCacheSnapshot<Set<String>> adminRolesSnapshot = new LocalCacheSnapshot<>("authorizations");

	@Autowired
	protected ApplicationContext applicationContext;

	@Override
	@CacheResult(cacheName = "user-details")
	public UserDetails loadUserByUsername(@CacheKey final String username) {
		final var userAndRoles = userRepository.findByLoginFetchRoles(username);
		final SystemUser user;
		final Collection<GrantedAuthority> authorities;
		if (userAndRoles.length == 0) {
			user = new SystemUser();
			user.setLogin(username);
			authorities = new ArrayList<>();
		} else {
			user = (SystemUser) userAndRoles[0][0];

			// Add all roles
			authorities = toSimpleRoles(userAndRoles, 1);
		}

		// Update last connection information only as needed for performance, delta is one day
		final var now = Instant.now();
		if (user.getLastConnection() == null || ChronoUnit.DAYS.between(user.getLastConnection(), now) >= 1) {
			user.setLastConnection(now);
			userRepository.saveAndFlush(user);
		}

		// Also add the default role as needed
		authorities.add(new SimpleGrantedAuthority(SystemRole.DEFAULT_ROLE));

		// Ask providers to complete the session details
		final var requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		final var request = requestAttributes.getRequest();
		if (!"true".equalsIgnoreCase(request.getHeader("x-api-local-roles"))) {
			// Ask providers to complete the session details
			applicationContext.getBeansOfType(ISessionSettingsProvider.class).values().forEach(p -> {
				final var addedRoles = p.getGrantedAuthorities(username);
				if (!addedRoles.isEmpty()) {
					log.debug("Add resolved roles {}", addedRoles);
					authorities.addAll(addedRoles);
				}
			});
		}

		// Resolve the administration access level: the principal is an administrator when one of the resolved
		// authorities (database roles or provider contributions) holds an administrative API authorization. Both the
		// virtual authority (for SpEL/authority based checks) and the precomputed flag (read by SecurityHelper) are set.
		// The administrator authority is only computed here, never granted by a role or a provider having its name
		authorities.removeIf(a -> SecurityHelper.ADMIN.equals(a.getAuthority()));
		final var adminRoles = adminRolesSnapshot.get(() -> {
			// Ensure the authorizations are cached: their eviction then invalidates this snapshot. Resolved lazily: an
			// injection would create the resource too early, before its cache and transaction proxies
			applicationContext.getBean(AuthorizationResource.class).getAuthorizations();
			return authorizationRepository.findAdminApiRoles();
		});
		final var admin = authorities.stream().anyMatch(a -> adminRoles.contains(a.getAuthority()));
		if (admin) {
			authorities.add(new SimpleGrantedAuthority(SecurityHelper.ADMIN));
		}

		return new RbacUserDetails(username, "N/A", admin, authorities);
	}

	/**
	 * Extract fetched elements from a multi-select.
	 *
	 * @param results the ResultSet of multi-select.
	 * @param index   data index to extract.
	 * @return the collected role names.
	 */
	private Set<GrantedAuthority> toSimpleRoles(final Object[][] results, final int index) {
		final var result = new HashSet<GrantedAuthority>();
		final var resultAsName = new HashSet<String>();
		for (final var object : results) {
			final var role = (SystemRole) object[index];
			if (role != null && resultAsName.add(role.getName())) {
				result.add(new SimpleGrantedAuthority(role.getAuthority()));
			}
		}
		return result;
	}

}
