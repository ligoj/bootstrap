/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import java.util.Objects;

/**
 * Authentication based on API token. It is saved with a salt, and associated to a user.
 */
@Setter
public class ApiTokenAuthenticationFilter extends RequestHeaderAuthenticationFilter {

	@Autowired
	private ApiTokenResource resource;

	/**
	 * Default constructor with default credential header.
	 */
	public ApiTokenAuthenticationFilter() {
		setCredentialsRequestHeader("x-api-key");
	}

	/**
	 * Return the user corresponding to the given API Token.
	 *
	 * @param request the current request.
	 * @return the current user or <code>null</code> is no match found.
	 */
	@Override
	protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
		final var principal = (String) super.getPreAuthenticatedPrincipal(request);
		final var credential = (String) super.getPreAuthenticatedCredentials(request);
		if (principal == null || credential == null || resource.check(Objects.requireNonNullElse(request.getHeader("x-api-via-user"), principal), credential)) {
			return principal;
		}

		// Credential has not been validated, the user is invalid
		return null;
	}

	/**
	 * Credentials aren't usually applicable, but if a {@code credentialsRequestHeader} is
	 * set, this will be read and used as the credentials value. Otherwise, a dummy not <code>null</code> value
	 * will be used.
	 */
	@Override
	protected Object getPreAuthenticatedCredentials(final HttpServletRequest request) {
		return ObjectUtils.defaultIfNull(super.getPreAuthenticatedCredentials(request), "N/A");
	}
}
