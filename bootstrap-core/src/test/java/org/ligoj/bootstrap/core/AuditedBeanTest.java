/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.model.AbstractAudited;
import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;
import org.ligoj.bootstrap.core.model.Auditable;

import java.util.Date;

/**
 * {@link AuditedBean}, {@link AbstractAudited},
 * {@link AbstractNamedAuditedEntity} test class.
 */
class AuditedBeanTest {

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable, Auditable)}
	 */
	@Test
	void testCopyAuditData() {
		final var from = newAuditable();
		final AbstractNamedAuditedEntity<Integer> to = new AbstractNamedAuditedEntity<>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;

			// Nothing
		};
		to.setName("two");
		AuditedBean.copyAuditData(from, to);
		assertData(to);
		Assertions.assertEquals("two", to.getName());
		Assertions.assertEquals(0, to.compareTo(to));
		Assertions.assertTrue(to.toString().endsWith("(name=two)"));
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable, Auditable)}
	 */
	@Test
	void testToString() {
		final var auditedBean = new AuditedBean<>();
		auditedBean.setId(0);
		Assertions.assertEquals("AuditedBean(id=0)", auditedBean.toString());
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable, Auditable)}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testCopyAuditDataFromNull() {
		final Auditable<String, ?, Date> to = newAuditable();
		AuditedBean.copyAuditData(null, (Auditable) to);
		assertData(to);
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable)}
	 */
	@Test
	void testCopyAuditDataInstanceFromNull() {
		final var audited = newAuditedVo();
		audited.copyAuditData(null);
		assertData(audited);
	}

	/**
	 * Test {@link AuditedBean#getCreatedDate()} and
	 * {@link AuditedBean#getLastModifiedDate()}
	 */
	@Test
	void testGetDateNull() {
		final Auditable<String, Integer, Date> from = new AbstractAudited<>() {
			// Nothing
		};
		final var audited = new AuditedBean<String, Integer>();
		audited.copyAuditData(from);
		Assertions.assertNull(audited.getCreatedDate());
		Assertions.assertNull(audited.getLastModifiedDate());
		Assertions.assertNull(audited.getCreationContext());
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable)}
	 */
	@Test
	void testCopyAuditDataInstance() {
		final var from = newAuditable();
		final var audited = new AuditedBean<String, Integer>();
		audited.copyAuditData(from);
		assertData(audited);
	}

	private AuditedBean<String, Integer> newAuditedVo() {
		final var audited = new AuditedBean<String, Integer>();
		audited.setCreatedBy("any");
		audited.setLastModifiedBy("one");
		audited.setCreatedDate(new Date(0));
		audited.setLastModifiedDate(new Date(1));
		audited.setCreationContext("new");
		return audited;
	}

	private Auditable<String, Integer, Date> newAuditable() {
		final Auditable<String, Integer, Date> from = new AbstractAudited<>() {
			// Nothing
		};
		from.setCreatedBy("any");
		from.setLastModifiedBy("one");
		from.setCreatedDate(new Date(0));
		from.setLastModifiedDate(new Date(1));
		from.setCreationContext("new");
		return from;
	}

	private void assertData(final Auditable<String, ?, Date> to) {
		Assertions.assertEquals("any", to.getCreatedBy());
		Assertions.assertEquals("one", to.getLastModifiedBy());
		Assertions.assertEquals(new Date(0), to.getCreatedDate());
		Assertions.assertEquals(new Date(1), to.getLastModifiedDate());
	}

	private void assertData(final AuditedBean<String, Integer> audited) {
		Assertions.assertEquals("any", audited.getCreatedBy());
		Assertions.assertEquals("one", audited.getLastModifiedBy());
		Assertions.assertEquals(new Date(0), audited.getCreatedDate());
		Assertions.assertEquals(new Date(1), audited.getLastModifiedDate());
		Assertions.assertEquals("new", audited.getCreationContext());
	}
}