/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.dao.system.SystemUserRepository;
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
public class RoleResourceTest extends AbstractBootTest {

	@Autowired
	private RoleResource resource;

	@Autowired
	private SystemUserRepository userRepository;

	private String roleTestName;

	private Integer roleTestId;

	@BeforeEach
	public void setUp2() throws IOException {
		persistEntities(SystemRole.class, "csv/system-test/role.csv");
		persistEntities(SystemAuthorization.class, "csv/system-test/authorization.csv");
		persistEntities(SystemUser.class, "csv/system-test/user.csv");
		persistEntities(SystemRoleAssignment.class, "csv/system-test/role-assignment.csv");
		final SystemRole role = em.createQuery("FROM SystemRole", SystemRole.class).setMaxResults(1).getResultList().get(0);
		roleTestId = role.getId();
		roleTestName = role.getName();
		em.flush();
		em.clear();
	}

	/**
	 * test find all service
	 */
	@Test
	public void findAll() {
		final TableItem<SystemRole> result = resource.findAll();
		Assertions.assertEquals(5, result.getData().size());
	}

	/**
	 * test find all service
	 */
	@Test
	public void findAllFetchAuth() {
		final TableItem<SystemRoleVo> result = resource.findAllFetchAuth();
		Assertions.assertEquals(5, result.getData().size());
		Assertions.assertEquals(2, result.getData().get(0).getAuthorizations().size());
	}

	/**
	 * test find by id service
	 */
	@Test
	public void findById() {
		final SystemRole result = resource.findById(roleTestId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(roleTestName, result.getName());
	}

	/**
	 * test find by id service. Id is not in database
	 */
	@Test
	public void findByIdNotFound() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> {
			resource.findById(-1);
		});
	}

	/**
	 * test create service
	 */
	@Test
	public void create() {
		final int resultId = resource.create(newRoleVo());
		// check result
		em.flush();
		em.clear();
		final SystemRole result = em.find(SystemRole.class, resultId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals("TEST", result.getName());
		final SystemAuthorization auth = retrieveAuthQuery(result).getSingleResult();
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
		final SystemRoleVo roleVo = new SystemRoleVo();
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
		final AuthorizationEditionVo authorizationEditionVo = new AuthorizationEditionVo();
		authorizationEditionVo.setPattern(".*");
		authorizationEditionVo.setType(AuthorizationType.API);
		return authorizationEditionVo;
	}

	/**
	 * test update service
	 */
	@Test
	public void update() {
		final SystemRoleVo roleVo = newRoleVo();
		// test update name and add authorization
		roleVo.setId(roleTestId);
		resource.update(roleVo);
		// check result
		em.flush();
		em.clear();
		final SystemRole result = em.find(SystemRole.class, roleTestId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals("TEST", result.getName());
		final SystemAuthorization auth = retrieveAuthQuery(result).getSingleResult();
		Assertions.assertNotNull(auth);
		Assertions.assertEquals(".*", auth.getPattern());
		Assertions.assertEquals(AuthorizationType.API, auth.getType());

		// check keep existing auth
		roleVo.getAuthorizations().get(0).setId(auth.getId());
		roleVo.getAuthorizations().add(0, newAuthorization());
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
		return em.createQuery("FROM SystemAuthorization sa WHERE sa.role = :role", SystemAuthorization.class).setParameter("role", result);
	}

	/**
	 * test remove service
	 */
	@Test
	public void remove() {
		resource.remove(roleTestId);

		// check result
		em.flush();
		em.clear();
		Assertions.assertNull(em.find(SystemRole.class, roleTestId));
	}

	@Test
	public void isAdmin() {
		Assertions.assertTrue(userRepository.isAdmin("alongchu"));
		Assertions.assertFalse(userRepository.isAdmin("jdupont"));
	}

	@Test
	public void isAdminNoRole() {
		Assertions.assertFalse(userRepository.isAdmin("any"));
	}

	@Test
	public void isAdminOtherUser() {
		Assertions.assertFalse(userRepository.isAdmin("jdupont"));
	}
}
