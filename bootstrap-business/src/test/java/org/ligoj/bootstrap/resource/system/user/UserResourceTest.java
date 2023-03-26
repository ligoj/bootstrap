/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Test class of {@link UserResource}
 */
@ExtendWith(SpringExtension.class)
class UserResourceTest extends AbstractBootTest {

	@Autowired
	private UserResource resource;

	@Autowired
	private ApiTokenResource apiTokenResource;

	private int defaultRoleId;

	/**
	 * Push a Junit user
	 */
	@BeforeEach
	void prepare() {
		// Save ROLE
		final var newRole = new SystemRole();
		newRole.setName(DEFAULT_ROLE);
		em.persist(newRole);
		defaultRoleId = newRole.getId();
		// Save USER
		final var newUser = new SystemUser();
		newUser.setLogin(DEFAULT_USER);
		em.persist(newUser);

		// Save ROLE ASSIGNMENT
		final var newRoleAssignment = new SystemRoleAssignment();
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
	void findAll() {
		final var uriInfo = newUriInfo();
		final var users = resource.findAll(uriInfo);
		Assertions.assertEquals(1, users.getData().size());
		Assertions.assertEquals(DEFAULT_USER, users.getData().get(0));
	}

	@Test
	void findAllWithRoles() {
		final var uriInfo = newUriInfo();
		final var users = resource.findAllWithRoles(uriInfo);
		Assertions.assertEquals(1, users.getData().size());
		Assertions.assertEquals(DEFAULT_USER, users.getData().get(0).getLogin());
		Assertions.assertEquals(DEFAULT_ROLE, users.getData().get(0).getRoles().get(0).getName());
	}

	@Test
	void create() throws GeneralSecurityException {
		final var apiKey = resource.create(newUser());
		Assertions.assertNull(apiKey);
		Assertions.assertEquals("fdaugan", resource.findById("fdaugan").getLogin());
	}

	@Test
	void createWithApiKey() throws GeneralSecurityException {
		final var newUser = newUser();
		newUser.setApiToken("test");
		final var apiKey = resource.create(newUser);
		Assertions.assertNotNull(apiKey);
		Assertions.assertTrue(apiTokenResource.check(newUser.getLogin(), apiKey));
		Assertions.assertFalse(apiTokenResource.check(getAuthenticationName(), apiKey));
		Assertions.assertEquals("fdaugan", resource.findById("fdaugan").getLogin());
	}

	@Test
	void update() {
		// remove role
		final var userVo = defaultUser();
		resource.update(userVo);
		em.flush();
		em.clear();
		var userDto = resource.findById(DEFAULT_USER);
		Assertions.assertNotNull(userDto);
		Assertions.assertEquals(0, userDto.getRoles().size());
		// add role
		userVo.getRoles().add(defaultRoleId);
		resource.update(userVo);
		em.flush();
		em.clear();
		userDto = resource.findById(DEFAULT_USER);
		Assertions.assertNotNull(userDto);
		Assertions.assertEquals(1, userDto.getRoles().size());
		// nothing to do
		resource.update(userVo);
		em.flush();
		em.clear();
		userDto = resource.findById(DEFAULT_USER);
		Assertions.assertNotNull(userDto);
		Assertions.assertEquals(1, userDto.getRoles().size());
	}

	/**
	 * Check the user conversion.
	 */
	@Test
	void findById() {
		final var userDto = resource.findById(DEFAULT_USER);
		Assertions.assertNotNull(userDto);
		Assertions.assertEquals(DEFAULT_USER, userDto.getLogin());
	}

	/**
	 * Check the user deletion.
	 */
	@Test
	void delete() {
		resource.delete(DEFAULT_USER);
	}

	/**
	 * Create a new user object.
	 */
	private SystemUserEditionVo newUser() {
		final var userVo = new SystemUserEditionVo();
		userVo.setLogin("fdaugan");
		userVo.setRoles(Collections.singletonList(defaultRoleId));
		return userVo;
	}

	/**
	 * Create a default user object.
	 */
	private SystemUserEditionVo defaultUser() {
		final var userVo = new SystemUserEditionVo();
		userVo.setLogin(DEFAULT_USER);
		return userVo;
	}
}
