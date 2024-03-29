/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import java.util.ArrayList;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import lombok.extern.slf4j.Slf4j;

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
	 * Replace the username of current authentication by the given one. This is performed by creating a partial copy of
	 * current {@link SecurityContext}. Is equivalent to a "su" linux command, but without "exit" solution but calling
	 * again this function.
	 * 
	 * @param username
	 *            UserName to set.
	 * @return the previous {@link SecurityContext} to be able to restore it later.
	 */
	public SecurityContext setUserName(final String username) {
		if (username == null) {
			log.error("userName is needed in aim to update SecurityContext");
			return null;
		}
		final var context = SecurityContextHolder.getContext();
		final UserDetails newPrincipal = new User(username, "N/A", new ArrayList<>(0));
		replaceContext(context, newPrincipal);
		return context;
	}

	/**
	 * Put new information in SecurityContextHolder.
	 * 
	 * @param context
	 *            the security context to update.
	 * @param newPrincipal
	 *            the new principal to place.
	 */
	private void replaceContext(final SecurityContext context, final UserDetails newPrincipal) {
		final var authentication = new PreAuthenticatedAuthenticationToken(newPrincipal, null);
		authentication.setDetails(newPrincipal);
		context.setAuthentication(authentication);
		final var securityContextImpl = new SecurityContextImpl();
		securityContextImpl.setAuthentication(authentication);

		// Replace the old context
		SecurityContextHolder.setContext(securityContextImpl);
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
}
