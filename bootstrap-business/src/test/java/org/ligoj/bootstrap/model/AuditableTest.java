/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model;

import java.util.Date;

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
class AuditableTest extends AbstractBootTest {

	/**
	 * Fresh entity
	 */
	@Test
	void testNullAudit() {
		final var entity = new SystemBench();
		Assertions.assertNull(entity.getCreatedBy());
		Assertions.assertNull(entity.getCreatedDate());
		Assertions.assertNull(entity.getLastModifiedBy());
		Assertions.assertNull(entity.getLastModifiedDate());
		Assertions.assertNull(entity.getCreationContext());
	}

	/**
	 * Persisted entity test
	 */
	@Test
	void testAuditCreate() {
		SecurityContextHolder.clearContext();
		final var entity = new SystemBench();
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
	void testAuditUpdate() throws InterruptedException {
		final var entity = new SystemBench();
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
	void testAuditUpdateWithNew() throws InterruptedException {
		
		// Initial save
		final var entity = new SystemBench();
		em.persist(entity);
		em.flush();
		em.clear();
		Thread.sleep(200); // NOSONAR -- Have to pause the thread for the test

		// Update from a new instance
		SecurityContextHolder.clearContext();
        var entityUpdate = new SystemBench();
		entityUpdate.setId(entity.getId());
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
	void testAuditVo() {
		final var auditedVo = new AuditedBean<String, Integer>();
		final var object = new SystemBench();
		object.setCreatedBy(DEFAULT_USER);
		object.setLastModifiedBy(DEFAULT_ROLE);
		object.setCreatedDate(new Date());
		object.setLastModifiedDate(new Date());
		object.setCreationContext("SOME");
		auditedVo.copyAuditData(object);
		Assertions.assertEquals(DEFAULT_USER, auditedVo.getCreatedBy());
		Assertions.assertEquals(DEFAULT_ROLE, auditedVo.getLastModifiedBy());
		Assertions.assertEquals(object.getCreatedDate(), auditedVo.getCreatedDate());
		Assertions.assertEquals(object.getLastModifiedDate(), auditedVo.getLastModifiedDate());
		Assertions.assertEquals(object.getCreationContext(), auditedVo.getCreationContext());
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	void testAuditVoNull() {
		final var auditedVo = new AuditedBean<String, Integer>();
		auditedVo.copyAuditData(null);
		Assertions.assertNull(auditedVo.getCreatedBy());
		Assertions.assertNull(auditedVo.getLastModifiedBy());
		Assertions.assertNull(auditedVo.getCreatedDate());
		Assertions.assertNull(auditedVo.getLastModifiedDate());
		Assertions.assertNull(auditedVo.getCreationContext());
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	void testAuditVoNullCopy() {
		AuditedBean.copyAuditData(null, (Auditable<String, String, Date>) null);
	}

	/**
	 * Null Audit VO copy
	 */
	@Test
	void testAuditVoCopy() {
		final var from = new SystemBench();
		from.setCreatedBy(DEFAULT_USER);
		from.setLastModifiedBy(DEFAULT_ROLE);
		from.setCreatedDate(new Date());
		from.setLastModifiedDate(new Date());
		from.setCreationContext("SOME");
		final var to = new SystemBench();
		AuditedBean.copyAuditData(from, to);
		Assertions.assertEquals(DEFAULT_USER, to.getCreatedBy());
		Assertions.assertEquals(DEFAULT_ROLE, to.getLastModifiedBy());
		Assertions.assertEquals(from.getCreatedDate(), to.getCreatedDate());
		Assertions.assertEquals(from.getLastModifiedDate(), to.getLastModifiedDate());
		Assertions.assertEquals(from.getCreationContext(), to.getCreationContext());
	}

}
