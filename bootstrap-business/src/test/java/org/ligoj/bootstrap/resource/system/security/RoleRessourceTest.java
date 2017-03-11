package org.ligoj.bootstrap.resource.system.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.core.json.TableItem;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;

/**
 * Test class of {@link RoleResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/jpa-context-test.xml", "classpath:/META-INF/spring/business-context-test.xml",
		"classpath:/META-INF/spring/security-context-test.xml" })
@Rollback
@Transactional
public class RoleRessourceTest extends AbstractJpaTest {

	@Autowired
	private RoleResource resource;

	private String roleTestName;

	private Integer roleTestId;

	@Before
	public void setUp2() throws IOException {
		persistEntities(SystemRole.class, "csv/system-test/role.csv");
		persistEntities(SystemAuthorization.class, "csv/system-test/authorization.csv");
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
	public void testFindAll() {
		final TableItem<SystemRole> result = resource.findAll();
		Assert.assertEquals(5, result.getData().size());
	}

	/**
	 * test find all service
	 */
	@Test
	public void testFindAllFetchAuth() {
		final TableItem<SystemRoleVo> result = resource.findAllFetchAuth();
		Assert.assertEquals(5, result.getData().size());
		Assert.assertEquals(2, result.getData().get(0).getAuthorizations().size());
	}

	/**
	 * test find by id service
	 */
	@Test
	public void testFindById() {
		final SystemRole result = resource.findById(roleTestId);
		Assert.assertNotNull(result);
		Assert.assertEquals(roleTestName, result.getName());
	}

	/**
	 * test find by id service. Id is not in database
	 */
	@Test(expected = JpaObjectRetrievalFailureException.class)
	public void testFindByIdNotFound() {
		resource.findById(-1);
	}

	/**
	 * test create service
	 */
	@Test
	public void testCreate() {
		final int resultId = resource.create(newRoleVo());
		// check result
		em.flush();
		em.clear();
		final SystemRole result = em.find(SystemRole.class, resultId);
		Assert.assertNotNull(result);
		Assert.assertEquals("TEST", result.getName());
		final SystemAuthorization auth = retrieveAuthQuery(result).getSingleResult();
		Assert.assertNotNull(auth);
		Assert.assertEquals(".*", auth.getPattern());
		Assert.assertEquals(AuthorizationType.BUSINESS, auth.getType());
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
		authorizationEditionVo.setType(AuthorizationType.BUSINESS);
		return authorizationEditionVo;
	}

	/**
	 * test update service
	 */
	@Test
	public void testUpdate() {
		final SystemRoleVo roleVo = newRoleVo();
		// test update name and add authorization
		roleVo.setId(roleTestId);
		resource.update(roleVo);
		// check result
		em.flush();
		em.clear();
		final SystemRole result = em.find(SystemRole.class, roleTestId);
		Assert.assertNotNull(result);
		Assert.assertEquals("TEST", result.getName());
		final SystemAuthorization auth = retrieveAuthQuery(result).getSingleResult();
		Assert.assertNotNull(auth);
		Assert.assertEquals(".*", auth.getPattern());
		Assert.assertEquals(AuthorizationType.BUSINESS, auth.getType());

		// check keep existing auth
		roleVo.getAuthorizations().get(0).setId(auth.getId());
		roleVo.getAuthorizations().add(0, newAuthorization());
		resource.update(roleVo);
		// check result
		em.flush();
		em.clear();
		Assert.assertEquals(2, retrieveAuthQuery(result).getResultList().size());

		// check remove auth
		roleVo.getAuthorizations().clear();
		resource.update(roleVo);
		// check result
		em.flush();
		em.clear();
		Assert.assertTrue(retrieveAuthQuery(result).getResultList().isEmpty());
	}

	private TypedQuery<SystemAuthorization> retrieveAuthQuery(final SystemRole result) {
		return em.createQuery("FROM SystemAuthorization sa WHERE sa.role = :role", SystemAuthorization.class).setParameter("role", result);
	}

	/**
	 * test remove service
	 */
	@Test
	public void testRemove() {
		resource.remove(roleTestId);

		// check result
		em.flush();
		em.clear();
		Assert.assertNull(em.find(SystemRole.class, roleTestId));
	}
}
