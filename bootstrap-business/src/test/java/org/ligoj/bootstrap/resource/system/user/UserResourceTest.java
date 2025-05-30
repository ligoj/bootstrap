/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.api.ApiTokenResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * Test class of {@link UserResource}
 */
@ExtendWith(SpringExtension.class)
class UserResourceTest extends AbstractBootTest {

	@Autowired
	private UserResource resource;

	@Autowired
	private ApiTokenResource apiTokenResource;

	@Autowired
	private SystemRoleRepository roleRepository;

	@Autowired
	private SystemUserRepository userRepository;

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
		Assertions.assertEquals(DEFAULT_USER, users.getData().getFirst());
	}

	@Test
	void findAllWithRoles() {

		// Add duplicated role
		final var assignment  =new SystemRoleAssignment();
		assignment.setRole(roleRepository.findByName(DEFAULT_ROLE));
		assignment.setUser(userRepository.findOne(DEFAULT_USER));
		em.persist(assignment);

		final var uriInfo = newUriInfo();
		final var users = resource.findAllWithRoles(uriInfo);
		Assertions.assertEquals(1, users.getData().size());
		Assertions.assertEquals(DEFAULT_USER, users.getData().getFirst().getLogin());
		Assertions.assertEquals(DEFAULT_ROLE, users.getData().getFirst().getRoles().getFirst().getName());
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
		final var apiKey = resource.create(newUser).getId();
		Assertions.assertNotNull(apiKey);
		Assertions.assertTrue(apiTokenResource.check(newUser.getLogin(), apiKey));
		Assertions.assertFalse(apiTokenResource.check(getAuthenticationName(), apiKey));
		Assertions.assertEquals("fdaugan", resource.findById("fdaugan").getLogin());

		// Second creation does not return the previous or new token
		Assertions.assertNull(resource.create(newUser));
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

		// Add role
		final var newRole = newRole("test_role");
		userVo.getRoles().add(defaultRoleId);
		userVo.getRoles().add(newRole);
		resource.update(userVo);
		em.flush();
		em.clear();
		userDto = resource.findById(DEFAULT_USER);
		Assertions.assertNotNull(userDto);
		Assertions.assertEquals(2, userDto.getRoles().size());
		Assertions.assertTrue(userDto.getRoles().stream().anyMatch(r -> r.getRole().getId().equals(newRole)));
		Assertions.assertTrue(userDto.getRoles().stream().anyMatch(r -> r.getRole().getId().equals(defaultRoleId)));

		// nothing to do
		resource.update(userVo);
		em.flush();
		em.clear();
		userDto = resource.findById(DEFAULT_USER);
		Assertions.assertNotNull(userDto);
		Assertions.assertEquals(2, userDto.getRoles().size());

		// Remove role
		userVo.getRoles().remove(newRole);
		resource.update(userVo);
		em.flush();
		em.clear();
		userDto = resource.findById(DEFAULT_USER);
		Assertions.assertNotNull(userDto);
		Assertions.assertEquals(1, userDto.getRoles().size());
		Assertions.assertTrue(userDto.getRoles().stream().anyMatch(r -> r.getRole().getId().equals(defaultRoleId)));
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
		userVo.setRoles(Set.of(defaultRoleId));
		return userVo;
	}

	private int newRole(final String name) {
		final var role = new SystemRole();
		role.setName(name);
		roleRepository.saveAndFlush(role);
		return role.getId();
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
