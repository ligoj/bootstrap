package org.ligoj.bootstrap.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import org.ligoj.bootstrap.core.SpringUtils;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Basic user details service.
 */
@Component
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService, InitializingBean {

	/**
	 * User repository.
	 */
	private SystemUserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(final String username) {
		final Object[][] userAndRoles = userRepository.findByLoginFetchRoles(username);
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
		final Date now = org.ligoj.bootstrap.core.DateUtils.newCalendar().getTime();
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
	 * @param resultset
	 *            the ResultSet of multi-select.
	 * @param index
	 *            data index to extract.
	 * @return the collected role names.
	 */
	private Set<GrantedAuthority> toSimpleRoles(final Object[][] resultset, final int index) {
		final Set<GrantedAuthority> result = new HashSet<>();
		for (final Object[] object : resultset) {
			final SystemRole role = (SystemRole) object[index];
			if (role != null) {
				result.add(new SimpleGrantedAuthority(role.getAuthority()));
			}
		}
		return result;
	}

	@Override
	public void afterPropertiesSet() {
		userRepository = SpringUtils.getBean(SystemUserRepository.class);
	}

}
