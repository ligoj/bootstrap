package org.ligoj.bootstrap.resource.system.security;

import java.io.IOException;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.transaction.Transactional;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.cache.CacheResource;

/**
 * Test class of {@link AuthorizationResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class AuthorizationResourceTest extends AbstractJpaTest {

	@Autowired
	private AuthorizationResource resource;
	
	@Autowired
	private CacheResource cacheResource;

	@Autowired
	private SystemRoleRepository systemRoleRepository;

	private Integer authorizationId;

	@Before
	public void setUp2() throws IOException {
		persistEntities(SystemRole.class, "csv/system-test/role.csv");
		persistEntities(SystemUser.class, "csv/system-test/user.csv");
		persistEntities(SystemAuthorization.class, "csv/system-test/authorization.csv");
		persistEntities(SystemRoleAssignment.class, "csv/system-test/role-assignment.csv");
		authorizationId = em.createQuery("FROM SystemAuthorization", SystemAuthorization.class).setMaxResults(1).getResultList().get(0).getId();
	}

	/**
	 * test find by id service
	 */
	@Test
	public void testFindById() {
		final SystemAuthorization result = resource.findById(authorizationId);

		// Also check the lazy lading issue
		em.clear();
		Assert.assertNotNull(result);
		Assert.assertEquals(authorizationId, result.getId());
		Assert.assertNotNull(result.getRole());
	}

	/**
	 * test find by user for UI authorization.
	 */
	@Test
	public void testFindByUserLoginUI() {
		final SystemRole role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);
		final SystemRole role2 = new SystemRole();
		role2.setName(DEFAULT_ROLE + 2);
		em.persist(role2);
		final SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

		final SystemRoleAssignment assignment1 = new SystemRoleAssignment();
		assignment1.setRole(role);
		assignment1.setUser(user);
		em.persist(assignment1);

		final SystemRoleAssignment assignment2 = new SystemRoleAssignment();
		assignment2.setRole(role2);
		assignment2.setUser(user);
		em.persist(assignment2);

		final SystemAuthorization authorization = new SystemAuthorization();
		authorization.setType(AuthorizationType.UI);
		authorization.setRole(role);
		authorization.setPattern("pattern");
		em.persist(authorization);

		em.flush();
		em.clear();

		final List<SystemAuthorization> result = resource.findAuthorizationsUi(getJaxRsSecurityContext(DEFAULT_USER));

		// Also check the lazy lading issue
		em.clear();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(authorization.getId(), result.get(0).getId());
		Assert.assertEquals(role.getId(), result.get(0).getRole().getId());
		Assert.assertEquals(AuthorizationType.UI, result.get(0).getType());
		Assert.assertEquals("pattern", result.get(0).getPattern());
	}

	/**
	 * test find by user for business authorization.
	 */
	@Test
	public void testFindByUserLoginBusiness() {
		final SystemRole role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);
		final SystemRole role2 = new SystemRole();
		role2.setName(DEFAULT_ROLE + 2);
		em.persist(role2);
		final SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

		final SystemRoleAssignment assignment1 = new SystemRoleAssignment();
		assignment1.setRole(role);
		assignment1.setUser(user);
		em.persist(assignment1);

		final SystemRoleAssignment assignment2 = new SystemRoleAssignment();
		assignment2.setRole(role2);
		assignment2.setUser(user);
		em.persist(assignment2);

		final SystemAuthorization authorization = new SystemAuthorization();
		authorization.setType(AuthorizationType.BUSINESS);
		authorization.setRole(role);
		authorization.setPattern("pattern");
		em.persist(authorization);

		em.flush();
		em.clear();

		final List<SystemAuthorization> result = resource.findAuthorizationsBusiness(getJaxRsSecurityContext(DEFAULT_USER));

		// Also check the lazy lading issue
		em.clear();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(authorization.getId(), result.get(0).getId());
		Assert.assertEquals(role.getId(), result.get(0).getRole().getId());
		Assert.assertEquals(AuthorizationType.BUSINESS, result.get(0).getType());
		Assert.assertEquals("pattern", result.get(0).getPattern());
	}

	/**
	 * test create service
	 */
	@Test
	public void testCreateBusiness() {
		final SystemRole role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);
		em.flush();
		em.clear();
		final AuthorizationEditionVo authorization = new AuthorizationEditionVo();
		authorization.setType(AuthorizationType.UI);
		authorization.setRole(role.getId());
		authorization.setPattern("pattern");
		final int resultId = resource.create(authorization);

		// check result
		em.flush();
		em.clear();
		final SystemAuthorization result = em.find(SystemAuthorization.class, resultId);
		Assert.assertNotNull(result);
		Assert.assertEquals(role.getId(), result.getRole().getId());
		Assert.assertEquals(AuthorizationType.UI, result.getType());
		Assert.assertEquals("pattern", result.getPattern());
	}

	/**
	 * test create service without a valid role.
	 */
	@Test(expected = JpaObjectRetrievalFailureException.class)
	public void testCreateNoRole() {
		final AuthorizationEditionVo authorization = new AuthorizationEditionVo();
		authorization.setRole(-1);
		authorization.setPattern("any");
		resource.create(authorization);
	}

	/**
	 * test update service
	 */
	@Test
	public void testUpdateBusiness() {
		final SystemRole role2 = new SystemRole();
		role2.setName(DEFAULT_ROLE + "2");
		em.persist(role2);
		em.flush();
		em.clear();

		final AuthorizationEditionVo authorizationUpdate = new AuthorizationEditionVo();
		authorizationUpdate.setType(AuthorizationType.BUSINESS);
		authorizationUpdate.setRole(role2.getId());
		authorizationUpdate.setPattern("pattern");
		resource.update(authorizationId, authorizationUpdate);

		// check result
		em.flush();
		em.clear();
		final SystemAuthorization result = em.find(SystemAuthorization.class, authorizationId);
		Assert.assertNotNull(result);
		Assert.assertEquals(role2.getName(), result.getRole().getName());
		Assert.assertEquals("pattern", result.getPattern());
	}

	/**
	 * test update service without a valid role.
	 */
	@Test(expected = JpaObjectRetrievalFailureException.class)
	public void testUpdateNoRole() {
		final AuthorizationEditionVo authorization = new AuthorizationEditionVo();
		authorization.setRole(-1);
		authorization.setPattern("pattern");
		resource.update(authorizationId, authorization);
	}

	/**
	 * test update service without a valid identifier.
	 */
	@Test(expected = JpaObjectRetrievalFailureException.class)
	public void testUpdateInvalidId() {
		resource.update(-1, null);
	}

	/**
	 * Synchronized/invalid pattern test
	 */
	@Test(expected = PatternSyntaxException.class)
	public void testInvalidPatternFromDb() {
		addSystemAuthorization(HttpMethod.GET, "role1", "^(my\\(url");
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		resource.getAuthorizations();
	}

	private void addSystemAuthorization(final HttpMethod method, final String roleName, final String pattern) {
		SystemRole role = systemRoleRepository.findByName(roleName);
		if (role == null) {
			role = new SystemRole();
			role.setName(roleName);
			em.persist(role);
		}
		final SystemAuthorization authorization = new SystemAuthorization();
		authorization.setRole(role);
		authorization.setMethod(method);
		authorization.setType(AuthorizationType.BUSINESS);
		authorization.setPattern(pattern);
		em.persist(authorization);
	}

	/**
	 * test remove service
	 */
	@Test
	public void testRemove() {
		resource.remove(authorizationId);

		// check result
		em.flush();
		em.clear();
		Assert.assertNull(em.find(SystemAuthorization.class, authorizationId));
	}
}
