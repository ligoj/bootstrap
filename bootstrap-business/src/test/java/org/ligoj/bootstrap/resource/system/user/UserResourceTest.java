package org.ligoj.bootstrap.resource.system.user;

import java.util.Collections;

import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class of {@link UserResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class UserResourceTest extends AbstractBootTest {

	@Autowired
	private UserResource resource;

	private int defaultRoleId;

	/**
	 * Push a Junit user
	 */
	@Before
	public void prepare() {
		// Save ROLE
		final SystemRole newRole = new SystemRole();
		newRole.setName(DEFAULT_ROLE);
		em.persist(newRole);
		defaultRoleId = newRole.getId();
		// Save USER
		final SystemUser newUser = new SystemUser();
		newUser.setLogin(DEFAULT_USER);
		em.persist(newUser);

		// Save ROLE ASSIGNMENT
		final SystemRoleAssignment newRoleAssignment = new SystemRoleAssignment();
		newRoleAssignment.setUser(newUser);
		newRoleAssignment.setRole(newRole);
		em.persist(newRoleAssignment);

		em.flush();
		em.clear();
	}

	/**
	 * Find all users, without using explicit paging feature.
	 */
	@Test
	public void findAll() {
		final UriInfo uriInfo = newUriInfo();
		final TableItem<String> users = resource.findAll(uriInfo);
		Assert.assertEquals(1, users.getData().size());
		Assert.assertEquals(DEFAULT_USER, users.getData().get(0));
	}

	@Test
	public void findAllWithRoles() {
		final UriInfo uriInfo = newUriInfo();
		final TableItem<SystemUserVo> users = resource.findAllWithRoles(uriInfo);
		Assert.assertEquals(1, users.getData().size());
		Assert.assertEquals(DEFAULT_USER, users.getData().get(0).getLogin());
		Assert.assertEquals(DEFAULT_ROLE, users.getData().get(0).getRoles().get(0).getName());
	}

	@Test
	public void create() {
		resource.create(newUser());
		Assert.assertEquals("fdaugan", resource.findById("fdaugan").getLogin());
	}

	@Test
	public void update() {
		// remove role
		final SystemUserEditionVo userVo = defaultUser();
		resource.update(userVo);
		em.flush();
		em.clear();
		SystemUser userDto = resource.findById(DEFAULT_USER);
		Assert.assertNotNull(userDto);
		Assert.assertEquals(0, userDto.getRoles().size());
		// add role
		userVo.getRoles().add(defaultRoleId);
		resource.update(userVo);
		em.flush();
		em.clear();
		userDto = resource.findById(DEFAULT_USER);
		Assert.assertNotNull(userDto);
		Assert.assertEquals(1, userDto.getRoles().size());
		// nothing to do
		resource.update(userVo);
		em.flush();
		em.clear();
		userDto = resource.findById(DEFAULT_USER);
		Assert.assertNotNull(userDto);
		Assert.assertEquals(1, userDto.getRoles().size());
	}

	/**
	 * Check the user conversion.
	 */
	@Test
	public void findById() {
		final SystemUser userDto = resource.findById(DEFAULT_USER);
		Assert.assertNotNull(userDto);
		Assert.assertEquals(DEFAULT_USER, userDto.getLogin());
	}

	/**
	 * Check the user deletion.
	 */
	@Test
	public void delete() {
		resource.delete(DEFAULT_USER);
	}

	/**
	 * Create a new user object.
	 */
	private SystemUserEditionVo newUser() {
		final SystemUserEditionVo userVo = new SystemUserEditionVo();
		userVo.setLogin("fdaugan");
		userVo.setRoles(Collections.singletonList(defaultRoleId));
		return userVo;
	}

	/**
	 * Create a default user object.
	 */
	private SystemUserEditionVo defaultUser() {
		final SystemUserEditionVo userVo = new SystemUserEditionVo();
		userVo.setLogin(DEFAULT_USER);
		return userVo;
	}
}
