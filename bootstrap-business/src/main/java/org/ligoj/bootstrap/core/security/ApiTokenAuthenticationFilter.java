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
 * Pre-authentication based on request headers: the principal header (configured with
 * {@link #setPrincipalRequestHeader(String)}, such as <code>SM_UNIVERSALID</code>) names the user, and the optional
 * <code>x-api-key</code> header holds an API token of this user.
 * <p>
 * <strong>Security requirement:</strong> when the <code>x-api-key</code> header is absent, the principal header is
 * trusted as is, without any other check. This mode is designed for an authenticating reverse proxy (SSO gateway)
 * placed in front of the application, and is only safe when all the following conditions hold:
 * <ul>
 * <li>The application is never reachable without going through this proxy: bind it to a private interface or
 * network, firewall its port.</li>
 * <li>The proxy removes or overwrites the principal header of every incoming request, so a client cannot forge
 * it.</li>
 * <li>The proxy also removes the <code>x-api-via-user</code> and <code>x-api-local-roles</code> client headers
 * unless the API delegation feature is used.</li>
 * </ul>
 * Otherwise, any client sending <code>SM_UNIVERSALID: admin</code> is authenticated as <code>admin</code>.
 * <p>
 * When the <code>x-api-key</code> header is present, the token is checked against the user named by the
 * <code>x-api-via-user</code> header when set (delegation: the token owner acts as the principal, accepted by
 * {@link AuthorizingFilter} only when the token owner is an administrator), against the principal otherwise.
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
		return ObjectUtils.getIfNull(super.getPreAuthenticatedCredentials(request), "N/A");
	}
}
