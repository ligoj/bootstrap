/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * Basic Security mock setup.
 */
public abstract class AbstractSecurityTest extends AbstractDataGeneratorTest {

	protected static final String DEFAULT_USER = "junit";
	protected static final String DEFAULT_ROLE = "junit_R";

	/**
	 * Shared logger for test.
	 */
	protected final Logger log = LoggerFactory.getLogger(this.getClass()); // NOPMD NOSONAR

	/**
	 * Tar user_details for NA
	 */
	private static final String USER_DETAILS_NA = "N/A";

	/**
	 * Prepare the Spring Security in the context, not the REST one.
	 */
	@BeforeEach
	public void setUp() {
		initSpringSecurityContext(getAuthenticationName());
	}

	/**
	 * Initialize {@link SecurityContextHolder} for given user.
	 * 
	 * @param user
	 *            the user to set in the context.
	 * @param authorities
	 *            the optional authorities name
	 * @return The configured {@link SecurityContext}.
	 */
	@SuppressWarnings("unchecked")
	protected SecurityContext initSpringSecurityContext(final String user, final GrantedAuthority... authorities) {
		SecurityContextHolder.clearContext();
		final var context = Mockito.mock(SecurityContext.class);
		final var authentication = Mockito.mock(Authentication.class);
		final var authoritiesAsList = Arrays.asList(authorities);
		final var userDetails = new User(user, USER_DETAILS_NA, authoritiesAsList);
		Mockito.when((List<GrantedAuthority>) authentication.getAuthorities()).thenReturn(authoritiesAsList);
		Mockito.when(context.getAuthentication()).thenReturn(authentication);
		Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
		Mockito.when(authentication.getName()).thenReturn(user);
		SecurityContextHolder.setContext(context);
		return context;
	}

	/**
	 * Cleanup security context.
	 */
	@AfterEach
	public void cleanup() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * Return the authentication name for mock security. May be overridden.
	 * 
	 * @return the user name used for Spring Security mocking.
	 */
    protected String getAuthenticationName() {
		return DEFAULT_USER;
	}

	/**
	 * Build and return a REST security context.
	 * 
	 * @param user
	 *            the user name of {@link javax.ws.rs.core.SecurityContext}
	 * @return a mock of {@link javax.ws.rs.core.SecurityContext} with a defined {@link Principal}
	 */
	protected javax.ws.rs.core.SecurityContext getJaxRsSecurityContext(final String user) {
		final var context = Mockito.mock(javax.ws.rs.core.SecurityContext.class);
		final var principal = Mockito.mock(Principal.class);
		Mockito.when(principal.getName()).thenReturn(user);
		Mockito.when(context.getUserPrincipal()).thenReturn(principal);
		return context;
	}

}
