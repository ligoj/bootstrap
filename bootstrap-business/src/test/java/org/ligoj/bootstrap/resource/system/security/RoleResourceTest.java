/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class of {@link RoleResource}
 */
@ExtendWith(SpringExtension.class)
class RoleResourceTest extends AbstractBootTest {

	@Autowired
	private RoleResource resource;

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
		Assertions.assertEquals(5, result.getRecordsTotal());
		Assertions.assertEquals(5, result.getRecordsFiltered());
		Assertions.assertEquals(2, result.getData().getFirst().getAuthorizations().size());

		final var role = result.getData().get(4);
		Assertions.assertEquals("Developer", role.getName());
		Assertions.assertEquals(4, role.getAuthorizations().size()); // No duplicated authorizations
		Assertions.assertEquals("GET", role.getAuthorizations().getFirst().getMethod()); // No duplicated authorizations
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
	void createReservedName() {
		// The names starting with '$' are reserved to the virtual authorities, such as the administrator one
		final var roleVo = newRoleVo();
		roleVo.setName("$admin");
		Assertions.assertThrows(org.ligoj.bootstrap.core.validation.ValidationJsonException.class, () -> resource.create(roleVo));
		roleVo.setId(roleTestId);
		Assertions.assertThrows(org.ligoj.bootstrap.core.validation.ValidationJsonException.class, () -> resource.update(roleVo));
	}

	@Test
	void create() {
		cacheManager.getCache("user-details").put("someone", "details");
		final var resultId = resource.create(newRoleVo());
		Assertions.assertNull(cacheManager.getCache("user-details").get("someone"));
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
		cacheManager.getCache("user-details").put("someone", "details");
		resource.update(roleVo);
		Assertions.assertNull(cacheManager.getCache("user-details").get("someone"));
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
		// The administrator flag of the users may change
		cacheManager.getCache("user-details").put("someone", "details");
		resource.remove(roleTestId);
		Assertions.assertNull(cacheManager.getCache("user-details").get("someone"));

		// Cached again before the commit by a concurrent request: cleared after the commit
		cacheManager.getCache("user-details").put("someone", "stale");
		org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations()
				.forEach(org.springframework.transaction.support.TransactionSynchronization::afterCommit);
		Assertions.assertNull(cacheManager.getCache("user-details").get("someone"));

		// check result
		em.flush();
		em.clear();
		Assertions.assertNull(em.find(SystemRole.class, roleTestId));
	}
}
