/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;

/**
 * Utility managing {@link SecurityContext}
 */
@Slf4j
public class SecurityHelper {

	/**
	 * The system username.
	 */
	public static final String SYSTEM_USERNAME = "_system";

	/**
	 * The virtual authority granted to administrators. A principal holding this authority has an administrative API
	 * authorization. It is granted by {@code RbacUserDetailsService} at authentication time, not stored in database.
	 *
	 * @see #isAdmin()
	 */
	public static final String ADMIN = "$admin";

	/**
	 * Replace the username of current authentication by the given one. This is performed by creating a partial copy of
	 * current {@link SecurityContext}. Is equivalent to a "su" linux command, but without "exit" solution but calling
	 * again this function.
	 * 
	 * @param username
	 *            UserName to set.
	 * @return the new current {@link SecurityContext}, or <code>null</code> when the username is <code>null</code>.
	 *         The previous context is not modified: to restore it, keep {@link SecurityContextHolder#getContext()}
	 *         before this call.
	 */
	public SecurityContext setUserName(final String username) {
		if (username == null) {
			log.error("userName is needed in aim to update SecurityContext");
			return null;
		}
		final UserDetails newPrincipal = new User(username, "N/A", new ArrayList<>(0));
		return replaceContext(newPrincipal);
	}

	/**
	 * Replace the current context by a new one with the given principal. The previous context, possibly held
	 * elsewhere (session, other thread), is not modified.
	 *
	 * @param newPrincipal
	 *            the new principal to place.
	 * @return The new context.
	 */
	private SecurityContext replaceContext(final UserDetails newPrincipal) {
		final var authentication = new PreAuthenticatedAuthenticationToken(newPrincipal, null);
		authentication.setDetails(newPrincipal);
		final var securityContextImpl = new SecurityContextImpl();
		securityContextImpl.setAuthentication(authentication);

		// Replace the old context
		SecurityContextHolder.setContext(securityContextImpl);
		return securityContextImpl;
	}

	/**
	 * Return the current username.
	 *
	 * @return the current username.
	 */
	public String getLogin() {
		final var context = SecurityContextHolder.getContext();
		if (context.getAuthentication() != null) {
			return context.getAuthentication().getName();
		}
		return null;
	}

	/**
	 * Indicate the current principal is an administrator. This is the truthful and complete administration access
	 * level: it accounts for all resolved authorities, including those not stored in database. This method is meant to
	 * be referenced from repository queries through the
	 * {@link org.ligoj.bootstrap.model.system.SystemUser#IS_ADMIN} SpEL bind parameter.
	 * <p>
	 * When the principal has been issued by {@code RbacUserDetailsService} (the regular authentication path), the
	 * decision reads the precomputed {@link RbacUserDetails#isAdmin()} flag without scanning the authorities. For any
	 * other principal (impersonation, tests, ...) it falls back to the {@value #ADMIN} virtual authority.
	 *
	 * @return <code>true</code> when the current principal is an administrator.
	 */
	public boolean isAdmin() {
		final var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return false;
		}
		if (authentication.getPrincipal() instanceof RbacUserDetails details) {
			return details.isAdmin();
		}
		// Fallback for principals not issued by RbacUserDetailsService
		return authentication.getAuthorities().stream().anyMatch(a -> ADMIN.equals(a.getAuthority()));
	}
}
