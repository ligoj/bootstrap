/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.session.ISessionSettingsProvider;
import org.ligoj.bootstrap.resource.system.session.SessionSettings;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

/**
 * {@link RbacUserDetailsService} test class.
 */
@ExtendWith(SpringExtension.class)
class RbacUserDetailsServiceTest extends AbstractBootTest {

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
				Math.abs(Instant.now().toEpochMilli() - user.getLastConnection().toEpochMilli()) < DateUtils.MILLIS_PER_MINUTE);
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
				Math.abs(Instant.now().toEpochMilli() - user.getLastConnection().toEpochMilli()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user and connected yesterday.
	 */
	@Test
	void testWellKnownUserYesterday() {
		var user = em.find(SystemUser.class, DEFAULT_USER);
		user.setLastConnection(Instant.now().minus(2, ChronoUnit.DAYS));
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
				Math.abs(Instant.now().toEpochMilli() - user.getLastConnection().toEpochMilli()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user and connected yesterday.
	 */
	@Test
	void testWellKnownUserNow() {
		var service = new RbacUserDetailsService();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(service);
		final var settings = new SessionSettings();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(settings);
		var applicationContext = Mockito.mock(ApplicationContext.class);
		service.applicationContext = applicationContext;
		Mockito.when(applicationContext.getBean(SessionSettings.class)).thenReturn(settings);
		var provider = Mockito.mock(ISessionSettingsProvider.class);
		Mockito.when(service.applicationContext.getBeansOfType(ISessionSettingsProvider.class))
				.thenReturn(Collections.singletonMap("provider", provider));

		final List<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("PLUGIN_ROLE"));
		Mockito.when(provider.getGrantedAuthorities(DEFAULT_USER)).thenReturn(roles);

		var user = em.find(SystemUser.class, DEFAULT_USER);
		user.setLastConnection(Instant.now().minusSeconds(1));
		final var expectedTime = user.getLastConnection();
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

		final var userDetails = service.loadUserByUsername(DEFAULT_USER);
		Assertions.assertEquals(DEFAULT_USER, userDetails.getUsername());
		user = em.find(SystemUser.class, DEFAULT_USER);
		Assertions.assertNotNull(user.getLastConnection());
		Assertions.assertEquals(expectedTime, user.getLastConnection());
		Assertions.assertEquals(3, userDetails.getAuthorities().size());
		final var iterator = userDetails.getAuthorities().iterator();
		Assertions.assertEquals("PLUGIN_ROLE", iterator.next().getAuthority());
		Assertions.assertEquals(SystemRole.DEFAULT_ROLE, iterator.next().getAuthority());
		Assertions.assertEquals(DEFAULT_ROLE, iterator.next().getAuthority());

	}

}
