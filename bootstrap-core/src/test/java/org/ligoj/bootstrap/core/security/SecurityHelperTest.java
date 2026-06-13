/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * {@link SecurityHelper} test class.
 */
class SecurityHelperTest {

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void cleanup() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * Test null username.
	 */
	@Test
	void setUserNameNull() {
		Assertions.assertNull(new SecurityHelper().setUserName(null));
	}

	/**
	 * Test username.
	 */
	@Test
	void setUserName() {
		final var sc = new SecurityHelper().setUserName("name");
		Assertions.assertEquals("name", sc.getAuthentication().getName());
		Assertions.assertEquals("name", new SecurityHelper().getLogin());
	}

	/**
	 * Test no login.
	 */
	@Test
	void getLogin() {
		Assertions.assertNull(new SecurityHelper().getLogin());
	}

	/**
	 * No authentication at all.
	 */
	@Test
	void isAdminNoContext() {
		Assertions.assertFalse(new SecurityHelper().isAdmin());
	}

	/**
	 * Authenticated, but without the administrator virtual authority.
	 */
	@Test
	void isAdminFalse() {
		authenticate("USER", "Manager");
		Assertions.assertFalse(new SecurityHelper().isAdmin());
	}

	/**
	 * Authenticated with the administrator virtual authority (fallback path, principal is not a {@link RbacUserDetails}).
	 */
	@Test
	void isAdmin() {
		authenticate("USER", SecurityHelper.ADMIN);
		Assertions.assertTrue(new SecurityHelper().isAdmin());
	}

	/**
	 * Principal issued by the authentication path: the decision reads the precomputed flag, not the authorities.
	 */
	@Test
	void isAdminFromFlag() {
		authenticatePrincipal(new RbacUserDetails("name", "N/A", true, List.of(new SimpleGrantedAuthority("USER"))));
		Assertions.assertTrue(new SecurityHelper().isAdmin());
	}

	/**
	 * The precomputed flag wins over the authorities: a {@link RbacUserDetails} flagged as non-administrator is not an
	 * administrator even if it holds the {@value SecurityHelper#ADMIN} authority.
	 */
	@Test
	void isAdminFromFlagWins() {
		authenticatePrincipal(
				new RbacUserDetails("name", "N/A", false, List.of(new SimpleGrantedAuthority(SecurityHelper.ADMIN))));
		Assertions.assertFalse(new SecurityHelper().isAdmin());
	}

	private void authenticate(final String... authorities) {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("name", "N/A",
				Stream.of(authorities).map(SimpleGrantedAuthority::new).toList()));
	}

	private void authenticatePrincipal(final RbacUserDetails principal) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, "N/A", principal.getAuthorities()));
	}

}
