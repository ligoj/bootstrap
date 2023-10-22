/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;

import org.apache.commons.lang3.time.DateUtils;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * User details service backed in database. All authenticated users get the role {@link SystemRole#DEFAULT_ROLE}
 */
@Component
public class RbacUserDetailsService implements UserDetailsService {

	/**
	 * User repository.
	 */
	@Autowired
	private SystemUserRepository userRepository;

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
		final var now = org.ligoj.bootstrap.core.DateUtils.newCalendar().getTime();
		if (user.getLastConnection() == null || now.getTime() - user.getLastConnection().getTime() > DateUtils.MILLIS_PER_DAY) {
			user.setLastConnection(now);
			userRepository.saveAndFlush(user);
		}

		// Also add the default role as needed
		authorities.add(new SimpleGrantedAuthority(SystemRole.DEFAULT_ROLE));
		return new User(username, "N/A", authorities);
	}

	/**
	 * Extract fetched elements from a multi-select.
	 * 
	 * @param results
	 *            the ResultSet of multi-select.
	 * @param index
	 *            data index to extract.
	 * @return the collected role names.
	 */
	private Set<GrantedAuthority> toSimpleRoles(final Object[][] results, final int index) {
		final var result = new HashSet<GrantedAuthority>();
		for (final var object : results) {
			final var role = (SystemRole) object[index];
			if (role != null) {
				result.add(new SimpleGrantedAuthority(role.getAuthority()));
			}
		}
		return result;
	}

}
