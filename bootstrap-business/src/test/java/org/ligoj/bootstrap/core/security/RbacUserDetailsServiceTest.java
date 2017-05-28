package org.ligoj.bootstrap.core.security;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * {@link RbacUserDetailsService} test class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RbacUserDetailsServiceTest extends AbstractBootTest {

	@Autowired
	private RbacUserDetailsService userDetailsService;

	@Before
	public void setup() {
		final SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);
		em.flush();
		em.clear();
	}

	/**
	 * Loading with an unknown user.
	 */
	@Test
	public void testUnknownUser() {
		final UserDetails userDetails = userDetailsService.loadUserByUsername("none");
		Assert.assertEquals(1, userDetails.getAuthorities().size());
		Assert.assertEquals(SystemRole.DEFAULT_ROLE, userDetails.getAuthorities().iterator().next().getAuthority());
		Assert.assertEquals("none", userDetails.getUsername());
		em.flush();
		final SystemUser user = em.find(SystemUser.class, "none");
		Assert.assertNotNull(user.getLastConnection());
		Assert.assertTrue(Math.abs(new Date().getTime() - user.getLastConnection().getTime()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user.
	 */
	@Test
	public void testWellKnownUser() {
		final UserDetails userDetails = userDetailsService.loadUserByUsername(DEFAULT_USER);
		Assert.assertEquals(1, userDetails.getAuthorities().size());
		Assert.assertEquals(SystemRole.DEFAULT_ROLE, userDetails.getAuthorities().iterator().next().getAuthority());
		Assert.assertEquals(DEFAULT_USER, userDetails.getUsername());
		final SystemUser user = em.find(SystemUser.class, DEFAULT_USER);
		Assert.assertNotNull(user.getLastConnection());
		Assert.assertTrue(Math.abs(new Date().getTime() - user.getLastConnection().getTime()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user and connected yesterday.
	 */
	@Test
	public void testWellKnownUserYesterday() {
		SystemUser user = em.find(SystemUser.class, DEFAULT_USER);
		user.setLastConnection(new Date(new Date().getTime() - DateUtils.MILLIS_PER_DAY * 2));
		em.persist(user);
		em.flush();
		em.clear();
		final UserDetails userDetails = userDetailsService.loadUserByUsername(DEFAULT_USER);
		Assert.assertEquals(1, userDetails.getAuthorities().size());
		Assert.assertEquals(SystemRole.DEFAULT_ROLE, userDetails.getAuthorities().iterator().next().getAuthority());
		Assert.assertEquals(DEFAULT_USER, userDetails.getUsername());
		user = em.find(SystemUser.class, DEFAULT_USER);
		Assert.assertNotNull(user.getLastConnection());
		Assert.assertTrue(Math.abs(new Date().getTime() - user.getLastConnection().getTime()) < DateUtils.MILLIS_PER_MINUTE);
	}

	/**
	 * Loading with a well known user and connected yesterday.
	 */
	@Test
	public void testWellKnownUserNow() {
		SystemUser user = em.find(SystemUser.class, DEFAULT_USER);
		user.setLastConnection(new Date(new Date().getTime() - DateUtils.MILLIS_PER_SECOND));
		final long expectedTime = user.getLastConnection().getTime();
		em.persist(user);

		final SystemRole role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);

		final SystemRoleAssignment roleAssignment = new SystemRoleAssignment();
		roleAssignment.setRole(role);
		roleAssignment.setUser(user);
		em.persist(roleAssignment);

		// Register another assignment but on the same role in order to check the DISTINCT
		final SystemRoleAssignment roleAssignment2 = new SystemRoleAssignment();
		roleAssignment2.setRole(role);
		roleAssignment2.setUser(user);
		em.persist(roleAssignment2);

		final UserDetails userDetails = userDetailsService.loadUserByUsername(DEFAULT_USER);
		Assert.assertEquals(DEFAULT_USER, userDetails.getUsername());
		user = em.find(SystemUser.class, DEFAULT_USER);
		Assert.assertNotNull(user.getLastConnection());
		Assert.assertEquals(expectedTime, user.getLastConnection().getTime());
		Assert.assertEquals(2, userDetails.getAuthorities().size());
		final Iterator<? extends GrantedAuthority> iterator = userDetails.getAuthorities().iterator();
		Assert.assertEquals(SystemRole.DEFAULT_ROLE, iterator.next().getAuthority());
		Assert.assertEquals(DEFAULT_ROLE, iterator.next().getAuthority());

	}

}
