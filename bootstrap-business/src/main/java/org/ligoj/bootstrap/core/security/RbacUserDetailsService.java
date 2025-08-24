/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.session.ISessionSettingsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

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
public class RbacUserDetailsService implements UserDetailsService {

	/**
	 * User repository.
	 */
	@Autowired
	private SystemUserRepository userRepository;

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

		// Update last connection information only as needed for performance, delta is one minute
		final var now = Instant.now();
		if (user.getLastConnection() == null || ChronoUnit.DAYS.between(user.getLastConnection(), now) >= 1) {
			user.setLastConnection(now);
			userRepository.saveAndFlush(user);
		}

		// Also add the default role as needed
		authorities.add(new SimpleGrantedAuthority(SystemRole.DEFAULT_ROLE));

		// Ask providers to complete the session details
		applicationContext.getBeansOfType(ISessionSettingsProvider.class).values().forEach(p -> authorities.addAll(p.getGrantedAuthorities(username)));

		return new User(username, "N/A", authorities);
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
