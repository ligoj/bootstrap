/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import java.io.IOException;
import java.util.regex.PatternSyntaxException;

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
import org.ligoj.bootstrap.resource.system.cache.CacheResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link AuthorizationResource}
 */
@ExtendWith(SpringExtension.class)
class AuthorizationResourceTest extends AbstractBootTest {

	@Autowired
	private AuthorizationResource resource;

	@Autowired
	private CacheResource cacheResource;

	@Autowired
	private SystemRoleRepository systemRoleRepository;

	private Integer authorizationId;

	@BeforeEach
	void setUp2() throws IOException {
		persistEntities(SystemRole.class, "csv/system-test/role.csv");
		persistEntities(SystemUser.class, "csv/system-test/user.csv");
		persistEntities(SystemAuthorization.class, "csv/system-test/authorization.csv");
		persistEntities(SystemRoleAssignment.class, "csv/system-test/role-assignment.csv");
		authorizationId = em.createQuery("FROM SystemAuthorization", SystemAuthorization.class).setMaxResults(1)
				.getResultList().get(0).getId();
	}

	/**
	 * test find by id service
	 */
	@Test
	void testFindById() {
		final var result = resource.findById(authorizationId);

		// Also check the lazy lading issue
		em.clear();
		Assertions.assertNotNull(result);
		Assertions.assertEquals(authorizationId, result.getId());
		Assertions.assertNotNull(result.getRole());
	}

	/**
	 * test find by user for UI authorization.
	 */
	@Test
	void testFindByUserLoginUI() {
		final var role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);
		final var role2 = new SystemRole();
		role2.setName(DEFAULT_ROLE + 2);
		em.persist(role2);
		final var user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

		final var assignment1 = new SystemRoleAssignment();
		assignment1.setRole(role);
		assignment1.setUser(user);
		em.persist(assignment1);

		final var assignment2 = new SystemRoleAssignment();
		assignment2.setRole(role2);
		assignment2.setUser(user);
		em.persist(assignment2);

		final var authorization = new SystemAuthorization();
		authorization.setType(AuthorizationType.UI);
		authorization.setRole(role);
		authorization.setPattern("pattern");
		em.persist(authorization);

		em.flush();
		em.clear();

		final var result = resource.findAuthorizationsUi(getJaxRsSecurityContext(DEFAULT_USER));

		// Also check the lazy lading issue
		em.clear();
		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals(authorization.getId(), result.get(0).getId());
		Assertions.assertEquals(role.getId(), result.get(0).getRole().getId());
		Assertions.assertEquals(AuthorizationType.UI, result.get(0).getType());
		Assertions.assertEquals("pattern", result.get(0).getPattern());
	}

	/**
	 * test find by user for API authorization.
	 */
	@Test
	void testFindByUserLoginBusiness() {
		final var role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);
		final var role2 = new SystemRole();
		role2.setName(DEFAULT_ROLE + 2);
		em.persist(role2);
		final var user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

		final var assignment1 = new SystemRoleAssignment();
		assignment1.setRole(role);
		assignment1.setUser(user);
		em.persist(assignment1);

		final var assignment2 = new SystemRoleAssignment();
		assignment2.setRole(role2);
		assignment2.setUser(user);
		em.persist(assignment2);

		final var authorization = new SystemAuthorization();
		authorization.setType(AuthorizationType.API);
		authorization.setRole(role);
		authorization.setPattern("pattern");
		em.persist(authorization);

		em.flush();
		em.clear();

		final var result = resource.findAuthorizationsApi(getJaxRsSecurityContext(DEFAULT_USER));

		// Also check the lazy lading issue
		em.clear();
		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals(authorization.getId(), result.get(0).getId());
		Assertions.assertEquals(role.getId(), result.get(0).getRole().getId());
		Assertions.assertEquals(AuthorizationType.API, result.get(0).getType());
		Assertions.assertEquals("pattern", result.get(0).getPattern());
	}

	/**
	 * test create service
	 */
	@Test
	void testCreateBusiness() {
		final var role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);
		em.flush();
		em.clear();
		final var authorization = new AuthorizationEditionVo();
		authorization.setType(AuthorizationType.UI);
		authorization.setRole(role.getId());
		authorization.setPattern("pattern");
		final var resultId = resource.create(authorization);

		// check result
		em.flush();
		em.clear();
		final var result = em.find(SystemAuthorization.class, resultId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(role.getId(), result.getRole().getId());
		Assertions.assertEquals(AuthorizationType.UI, result.getType());
		Assertions.assertEquals("pattern", result.getPattern());
	}

	/**
	 * test create service without a valid role.
	 */
	@Test
	void testCreateNoRole() {
		final var authorization = new AuthorizationEditionVo();
		authorization.setRole(-1);
		authorization.setPattern("any");
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> resource.create(authorization));
	}

	/**
	 * test update service
	 */
	@Test
	void testUpdateBusiness() {
		final var role2 = new SystemRole();
		role2.setName(DEFAULT_ROLE + "2");
		em.persist(role2);
		em.flush();
		em.clear();

		final var authorizationUpdate = new AuthorizationEditionVo();
		authorizationUpdate.setType(AuthorizationType.API);
		authorizationUpdate.setRole(role2.getId());
		authorizationUpdate.setPattern("pattern");
		resource.update(authorizationId, authorizationUpdate);

		// check result
		em.flush();
		em.clear();
		final var result = em.find(SystemAuthorization.class, authorizationId);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(role2.getName(), result.getRole().getName());
		Assertions.assertEquals("pattern", result.getPattern());
	}

	/**
	 * test update service without a valid role.
	 */
	@Test
	void testUpdateNoRole() {
		final var authorization = new AuthorizationEditionVo();
		authorization.setRole(-1);
		authorization.setPattern("pattern");
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class,
				() -> resource.update(authorizationId, authorization));
	}

	/**
	 * test update service without a valid identifier.
	 */
	@Test
	void testUpdateInvalidId() {
		Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> resource.update(-1, null));
	}

	/**
	 * Synchronized/invalid pattern test
	 */
	@Test
	void testInvalidPatternFromDb() {
		addSystemAuthorization(HttpMethod.GET, "role1", "^(my\\(url");
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		Assertions.assertThrows(PatternSyntaxException.class, () -> resource.getAuthorizations());
	}

	private void addSystemAuthorization(final HttpMethod method, final String roleName, final String pattern) {
        var role = systemRoleRepository.findByName(roleName);
		if (role == null) {
			role = new SystemRole();
			role.setName(roleName);
			em.persist(role);
		}
		final var authorization = new SystemAuthorization();
		authorization.setRole(role);
		authorization.setMethod(method);
		authorization.setType(AuthorizationType.API);
		authorization.setPattern(pattern);
		em.persist(authorization);
	}

	/**
	 * test remove service
	 */
	@Test
	void testRemove() {
		resource.remove(authorizationId);

		// check result
		em.flush();
		em.clear();
		Assertions.assertNull(em.find(SystemAuthorization.class, authorizationId));
	}
}
