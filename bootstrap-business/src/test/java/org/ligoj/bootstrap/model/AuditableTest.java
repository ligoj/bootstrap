package org.ligoj.bootstrap.model;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.AuditedBean;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.model.Auditable;
import org.ligoj.bootstrap.core.security.SecurityHelper;
import org.ligoj.bootstrap.model.system.SystemBench;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Audit test
 */
@ExtendWith(SpringExtension.class)
public class AuditableTest extends AbstractBootTest {

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
		Assertions.assertNull(entity.getCreatedBy());
		Assertions.assertNull(entity.getCreatedDate());
		Assertions.assertNull(entity.getLastModifiedBy());
		Assertions.assertNull(entity.getLastModifiedDate());
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
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, entity.getCreatedBy());
		Assertions.assertNotNull(entity.getCreatedDate());
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, entity.getLastModifiedBy());
		Assertions.assertNotNull(entity.getLastModifiedDate());
		Assertions.assertEquals(entity.getLastModifiedDate(), entity.getCreatedDate());
	}

	/**
	 * Updated entity test
	 */
	@Test
	public void testAuditUpdate() throws InterruptedException {
		final SystemBench entity = new SystemBench();
		em.persist(entity);
		em.flush();
		Thread.sleep(200); // NOSONAR -- Have to pause the thread for the test
		SecurityContextHolder.clearContext();
		entity.setPrfBool(true);
		em.flush();
		Assertions.assertEquals(DEFAULT_USER, entity.getCreatedBy());
		Assertions.assertNotNull(entity.getCreatedDate());
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, entity.getLastModifiedBy());
		Assertions.assertNotNull(entity.getLastModifiedDate());
		Assertions.assertNotEquals(entity.getLastModifiedDate(),entity.getCreatedDate());
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
		Thread.sleep(200); // NOSONAR -- Have to pause the thread for the test

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
		Assertions.assertEquals(DEFAULT_USER, entityUpdate.getCreatedBy());
		Assertions.assertNotNull(entityUpdate.getCreatedDate());
		Assertions.assertEquals(SecurityHelper.SYSTEM_USERNAME, entityUpdate.getLastModifiedBy());
		Assertions.assertNotNull(entityUpdate.getLastModifiedDate());
		Assertions.assertNotEquals(entityUpdate.getLastModifiedDate(),entityUpdate.getCreatedDate());
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
		object.setCreatedDate(new Date());
		object.setLastModifiedDate(new Date());
		auditedVo.copyAuditData(object);
		Assertions.assertEquals(DEFAULT_USER, auditedVo.getCreatedBy());
		Assertions.assertEquals(DEFAULT_ROLE, auditedVo.getLastModifiedBy());
		Assertions.assertEquals(object.getCreatedDate(), auditedVo.getCreatedDate());
		Assertions.assertEquals(object.getLastModifiedDate(), auditedVo.getLastModifiedDate());
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	public void testAuditVoNull() {
		final AuditedBean<String, Integer> auditedVo = new AuditedBean<>();
		auditedVo.copyAuditData(null);
		Assertions.assertNull(auditedVo.getCreatedBy());
		Assertions.assertNull(auditedVo.getLastModifiedBy());
		Assertions.assertNull(auditedVo.getCreatedDate());
		Assertions.assertNull(auditedVo.getLastModifiedDate());
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	public void testAuditVoNullCopy() {
		AuditedBean.copyAuditData(null, (Auditable<String, String, Date>) null);
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	public void testAuditVoCopy() {
		final SystemBench from = new SystemBench();
		from.setCreatedBy(DEFAULT_USER);
		from.setLastModifiedBy(DEFAULT_ROLE);
		from.setCreatedDate(new Date());
		from.setLastModifiedDate(new Date());
		final SystemBench to = new SystemBench();
		AuditedBean.copyAuditData(from, to);
		Assertions.assertEquals(DEFAULT_USER, to.getCreatedBy());
		Assertions.assertEquals(DEFAULT_ROLE, to.getLastModifiedBy());
		Assertions.assertEquals(from.getCreatedDate(), to.getCreatedDate());
		Assertions.assertEquals(from.getLastModifiedDate(), to.getLastModifiedDate());
	}

}
