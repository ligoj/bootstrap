/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * {@link RbacUserDetailsService} test class.
 */
@ExtendWith(SpringExtension.class)
public class RbacUserDetailsServiceTest extends AbstractBootTest {

	@Autowired
	private RbacUserDetailsService userDetailsService;

	@BeforeEach
	void setup() {
		clearAllCache();
		final var user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);
		em.flush();
		em.clear();
	}

	/**
	 * Loading with an unknown user.
	 */
	@Test
	void testUnknownUser() {
		final var userDetails = userDetailsService.loadUserByUsername("none");
		Assertions.assertEquals(1, userDetails.getAuthorities().size());
		Assertions.assertEquals(SystemRole.DEFAULT_ROLE, userDetails.getAuthorities().iterator().next().getAuthority());
		Assertions.assertEquals("none", userDetails.getUsername());
		em.flush();
		final var user = em.find(SystemUser.class, "none");
		Assertions.assertNotNull(user.getLastConnection());
		Assertions.assertTrue(
				Math.abs(new Date().getTime() - user.getLastConnection().getTime()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user.
	 */
	@Test
	void testWellKnownUser() {
		final var userDetails = userDetailsService.loadUserByUsername(DEFAULT_USER);
		Assertions.assertEquals(1, userDetails.getAuthorities().size());
		Assertions.assertEquals(SystemRole.DEFAULT_ROLE, userDetails.getAuthorities().iterator().next().getAuthority());
		Assertions.assertEquals(DEFAULT_USER, userDetails.getUsername());
		final var user = em.find(SystemUser.class, DEFAULT_USER);
		Assertions.assertNotNull(user.getLastConnection());
		Assertions.assertTrue(
				Math.abs(new Date().getTime() - user.getLastConnection().getTime()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user and connected yesterday.
	 */
	@Test
	void testWellKnownUserYesterday() {
        var user = em.find(SystemUser.class, DEFAULT_USER);
		user.setLastConnection(new Date(new Date().getTime() - DateUtils.MILLIS_PER_DAY * 2));
		em.persist(user);
		em.flush();
		em.clear();
		final var userDetails = userDetailsService.loadUserByUsername(DEFAULT_USER);
		Assertions.assertEquals(1, userDetails.getAuthorities().size());
		Assertions.assertEquals(SystemRole.DEFAULT_ROLE, userDetails.getAuthorities().iterator().next().getAuthority());
		Assertions.assertEquals(DEFAULT_USER, userDetails.getUsername());
		user = em.find(SystemUser.class, DEFAULT_USER);
		Assertions.assertNotNull(user.getLastConnection());
		Assertions.assertTrue(
				Math.abs(new Date().getTime() - user.getLastConnection().getTime()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user and connected yesterday.
	 */
	@Test
	void testWellKnownUserNow() {
        var user = em.find(SystemUser.class, DEFAULT_USER);
		user.setLastConnection(new Date(new Date().getTime() - DateUtils.MILLIS_PER_SECOND));
		final var expectedTime = user.getLastConnection().getTime();
		em.persist(user);

		final var role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);

		final var roleAssignment = new SystemRoleAssignment();
		roleAssignment.setRole(role);
		roleAssignment.setUser(user);
		em.persist(roleAssignment);

		// Register another assignment but on the same role in order to check the DISTINCT
		final var roleAssignment2 = new SystemRoleAssignment();
		roleAssignment2.setRole(role);
		roleAssignment2.setUser(user);
		em.persist(roleAssignment2);

		final var userDetails = userDetailsService.loadUserByUsername(DEFAULT_USER);
		Assertions.assertEquals(DEFAULT_USER, userDetails.getUsername());
		user = em.find(SystemUser.class, DEFAULT_USER);
		Assertions.assertNotNull(user.getLastConnection());
		Assertions.assertEquals(expectedTime, user.getLastConnection().getTime());
		Assertions.assertEquals(2, userDetails.getAuthorities().size());
		final var iterator = userDetails.getAuthorities().iterator();
		Assertions.assertEquals(SystemRole.DEFAULT_ROLE, iterator.next().getAuthority());
		Assertions.assertEquals(DEFAULT_ROLE, iterator.next().getAuthority());

	}

}
