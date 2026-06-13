/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;

/**
 * {@link User} carrying the precomputed administration access level of the principal. The {@link #isAdmin()} flag is
 * resolved once at authentication time (see {@code RbacUserDetailsService}) so that {@link SecurityHelper#isAdmin()}
 * does not have to scan the authorities on each call.
 */
public class RbacUserDetails extends User {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * <code>true</code> when this principal is an administrator.
	 */
	private final boolean admin;

	/**
	 * Full constructor.
	 *
	 * @param username    The username presented to the authentication provider.
	 * @param password    The password presented to the authentication provider.
	 * @param admin       <code>true</code> when this principal is an administrator.
	 * @param authorities The authorities granted to the user.
	 */
	public RbacUserDetails(final String username, final String password, final boolean admin,
			final Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.admin = admin;
	}

	/**
	 * Indicate this principal is an administrator. This is the truthful, precomputed administration access level.
	 *
	 * @return <code>true</code> when this principal is an administrator.
	 */
	public boolean isAdmin() {
		return admin;
	}
}
