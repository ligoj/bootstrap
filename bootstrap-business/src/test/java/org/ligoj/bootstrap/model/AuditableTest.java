package org.ligoj.bootstrap.model;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Auditable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.transaction.Transactional;

import org.ligoj.bootstrap.AbstractSecurityTest;
import org.ligoj.bootstrap.core.AuditedBean;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.model.system.SystemBench;

/**
 * Audit test
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/jpa-context-test.xml" })
@Rollback
@Transactional
public class AuditableTest extends AbstractSecurityTest {

	/**
	 * Entity manager.
	 */
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	private EntityManager em;

	/**
	 * Fresh entity
	 */
	@Test
	public void testNullAudit() {
		final SystemBench entity = new SystemBench();
		Assert.assertNull(entity.getCreatedBy());
		Assert.assertNull(entity.getCreatedDate());
		Assert.assertNull(entity.getLastModifiedBy());
		Assert.assertNull(entity.getLastModifiedDate());
	}

	/**
	 * Persisted entity test
	 */
	@Test
	public void testAuditCreate() {
		SecurityContextHolder.clearContext();
		final SystemBench entity = new SystemBench();
		em.persist(entity);
		em.flush();
		Assert.assertEquals(SecurityHelper.SYSTEM_USERNAME, entity.getCreatedBy());
		Assert.assertNotNull(entity.getCreatedDate());
		Assert.assertEquals(SecurityHelper.SYSTEM_USERNAME, entity.getLastModifiedBy());
		Assert.assertNotNull(entity.getLastModifiedDate());
		Assert.assertEquals(entity.getLastModifiedDate(), entity.getCreatedDate());
	}

	/**
	 * Updated entity test
	 */
	@Test
	public void testAuditUpdate() throws InterruptedException {
		final SystemBench entity = new SystemBench();
		em.persist(entity);
		em.flush();
		Thread.sleep(200);
		SecurityContextHolder.clearContext();
		entity.setPrfBool(true);
		em.flush();
		Assert.assertEquals(DEFAULT_USER, entity.getCreatedBy());
		Assert.assertNotNull(entity.getCreatedDate());
		Assert.assertEquals(SecurityHelper.SYSTEM_USERNAME, entity.getLastModifiedBy());
		Assert.assertNotNull(entity.getLastModifiedDate());
		Assert.assertNotEquals(entity.getLastModifiedDate(),entity.getCreatedDate());
	}

	/**
	 * Updated entity test
	 */
	@Test
	public void testAuditUpdateWithNew() throws InterruptedException {
		
		// Initial save
		final SystemBench entity = new SystemBench();
		em.persist(entity);
		em.flush();
		em.clear();
		Thread.sleep(200);

		// Update from a new instance
		SecurityContextHolder.clearContext();
		SystemBench entityUpdate = new SystemBench();
		entityUpdate.setId(entity.getId().intValue());
		entityUpdate.setPrfBool(true);
		em.merge(entityUpdate);
		em.flush();
		em.clear();

		// Reload the entity to check the state
		entityUpdate = em.find(SystemBench.class,entity.getId());
		Assert.assertEquals(DEFAULT_USER, entityUpdate.getCreatedBy());
		Assert.assertNotNull(entityUpdate.getCreatedDate());
		Assert.assertEquals(SecurityHelper.SYSTEM_USERNAME, entityUpdate.getLastModifiedBy());
		Assert.assertNotNull(entityUpdate.getLastModifiedDate());
		Assert.assertNotEquals(entityUpdate.getLastModifiedDate(),entityUpdate.getCreatedDate());
	}

	/**
	 * Audit VO copy
	 */
	@Test
	public void testAuditVo() {
		final AuditedBean<String, Integer> auditedVo = new AuditedBean<>();
		final SystemBench object = new SystemBench();
		object.setCreatedBy(DEFAULT_USER);
		object.setLastModifiedBy(DEFAULT_ROLE);
		object.setCreatedDate(new DateTime());
		object.setLastModifiedDate(new DateTime());
		auditedVo.copyAuditData(object);
		Assert.assertEquals(DEFAULT_USER, auditedVo.getCreatedBy());
		Assert.assertEquals(DEFAULT_ROLE, auditedVo.getLastModifiedBy());
		Assert.assertEquals(object.getCreatedDate().toDate(), auditedVo.getCreatedDate());
		Assert.assertEquals(object.getLastModifiedDate().toDate(), auditedVo.getLastModifiedDate());
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	public void testAuditVoNull() {
		final AuditedBean<String, Integer> auditedVo = new AuditedBean<>();
		auditedVo.copyAuditData(null);
		Assert.assertNull(auditedVo.getCreatedBy());
		Assert.assertNull(auditedVo.getLastModifiedBy());
		Assert.assertNull(auditedVo.getCreatedDate());
		Assert.assertNull(auditedVo.getLastModifiedDate());
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	public void testAuditVoNullCopy() {
		AuditedBean.copyAuditData(null, (Auditable<String, String>) null);
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	public void testAuditVoCopy() {
		final SystemBench from = new SystemBench();
		from.setCreatedBy(DEFAULT_USER);
		from.setLastModifiedBy(DEFAULT_ROLE);
		from.setCreatedDate(new DateTime());
		from.setLastModifiedDate(new DateTime());
		final SystemBench to = new SystemBench();
		AuditedBean.copyAuditData(from, to);
		Assert.assertEquals(DEFAULT_USER, to.getCreatedBy());
		Assert.assertEquals(DEFAULT_ROLE, to.getLastModifiedBy());
		Assert.assertEquals(from.getCreatedDate(), to.getCreatedDate());
		Assert.assertEquals(from.getLastModifiedDate(), to.getLastModifiedDate());
	}

}
