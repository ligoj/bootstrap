/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link RoleResource}
 */
@ExtendWith(SpringExtension.class)
class RoleResourceTest extends AbstractBootTest {

	@Autowired
	private RoleResource resource;

	@Autowired
	private SystemUserRepository userRepository;

	@Autowired
	private SystemRoleRepository roleRepository;

	private String roleTestName;

	private Integer roleTestId;

	@BeforeEach
	void setUp2() throws IOException {
		persistEntities(SystemRole.class, "csv/system-test/role.csv");
		persistEntities(SystemAuthorization.class, "csv/system-test/authorization.csv");
		persistEntities(SystemUser.class, "csv/system-test/user.csv");
		persistEntities(SystemRoleAssignment.class, "csv/system-test/role-assignment.csv");
		final var role = em.createQuery("FROM SystemRole", SystemRole.class).setMaxResults(1).getResultList()
				.getFirst();
		roleTestId = role.getId();
		roleTestName = role.getName();
		em.flush();
		em.clear();
	}

	/**
	 * test find all service
	 */
	@Test
	void findAll() {
		final var result = resource.findAll();
		Assertions.assertEquals(5, result.getData().size());
	}

	/**
	 * test find all service
	 */
	@Test
	void findAllFetchAuth() {

		// Add duplicated authorization
		final var auth  =new SystemAuthorization();
		auth.setRole(roleRepository.findByName("Developer"));
		auth.setPattern("^tech-dev");
		auth.setType(AuthorizationType.UI);
		em.persist(auth);

		final var result = resource.findAllFetchAuth();
		Assertions.assertEquals(5, result.getData().size());
		Assertions.assertEquals(2, result.getData().getFirst().getAuthorizations().size());

		final var role = result.getData().get(4);
		Assertions.assertEquals("Developer", role.getName());
		Assertions.assertEquals(4, role.getAuthorizations().size()); // No duplicated authorizations
	}

	/**
	 * test find by id service
	 */
	@Test
	void findById() {
		final var result = resource.findById(roleTestId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(roleTestName, result.getName());
	}

	/**
	 * test find by name service
	 */
	@Test
	void findByName() {
		final var result = resource.findByName(roleTestName);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(roleTestId, result.getId());
	}

	/**
	 * test find by id service. ID is not in database
	 */
	@Test
	void findByIdNotFound() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> resource.findById(-1));
	}

	/**
	 * test create service
	 */
	@Test
	void create() {
		final var resultId = resource.create(newRoleVo());
		// check result
		em.flush();
		em.clear();
		final var result = em.find(SystemRole.class, resultId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals("TEST", result.getName());
		final var auth = retrieveAuthQuery(result).getSingleResult();
		Assertions.assertNotNull(auth);
		Assertions.assertEquals(".*", auth.getPattern());
		Assertions.assertEquals(AuthorizationType.API, auth.getType());
	}

	/**
	 * create new roleVo
	 *
	 * @return roleVo
	 */
	private SystemRoleVo newRoleVo() {
		final var roleVo = new SystemRoleVo();
		roleVo.setName("TEST");
		final List<AuthorizationEditionVo> roles = new ArrayList<>();
		roles.add(newAuthorization());
		roleVo.setAuthorizations(roles);
		return roleVo;
	}

	/**
	 * create an authorization
	 *
	 * @return authorization
	 */
	private AuthorizationEditionVo newAuthorization() {
		final var authorizationEditionVo = new AuthorizationEditionVo();
		authorizationEditionVo.setPattern(".*");
		authorizationEditionVo.setType(AuthorizationType.API);
		return authorizationEditionVo;
	}

	/**
	 * test update service
	 */
	@Test
	void update() {
		final var roleVo = newRoleVo();
		// test update name and add authorization
		roleVo.setId(roleTestId);
		resource.update(roleVo);
		// check result
		em.flush();
		em.clear();
		final var result = em.find(SystemRole.class, roleTestId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals("TEST", result.getName());
		final var auth = retrieveAuthQuery(result).getSingleResult();
		Assertions.assertNotNull(auth);
		Assertions.assertEquals(".*", auth.getPattern());
		Assertions.assertEquals(AuthorizationType.API, auth.getType());

		// check keep existing auth
		roleVo.getAuthorizations().getFirst().setId(auth.getId());
		roleVo.getAuthorizations().addFirst(newAuthorization());
		resource.update(roleVo);
		// check result
		em.flush();
		em.clear();
		Assertions.assertEquals(2, retrieveAuthQuery(result).getResultList().size());

		// check remove auth
		roleVo.getAuthorizations().clear();
		resource.update(roleVo);
		// check result
		em.flush();
		em.clear();
		Assertions.assertTrue(retrieveAuthQuery(result).getResultList().isEmpty());
	}

	private TypedQuery<SystemAuthorization> retrieveAuthQuery(final SystemRole result) {
		return em.createQuery("FROM SystemAuthorization sa WHERE sa.role = :role", SystemAuthorization.class)
				.setParameter("role", result);
	}

	/**
	 * test remove service
	 */
	@Test
	void remove() {
		resource.remove(roleTestId);

		// check result
		em.flush();
		em.clear();
		Assertions.assertNull(em.find(SystemRole.class, roleTestId));
	}

	@Test
	void isAdmin() {
		Assertions.assertTrue(userRepository.isAdmin("admin-test"));
		Assertions.assertFalse(userRepository.isAdmin("jdupont"));
	}

	@Test
	void isAdminNoRole() {
		Assertions.assertFalse(userRepository.isAdmin("any"));
	}

	@Test
	void isAdminOtherUser() {
		Assertions.assertFalse(userRepository.isAdmin("jdupont"));
	}
}
