package org.ligoj.bootstrap.core;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Auditable;

import org.ligoj.bootstrap.core.model.AbstractAudited;
import org.ligoj.bootstrap.core.model.AbstractNamedAuditedEntity;

/**
 * {@link AuditedBean}, {@link AbstractAudited}, {@link AbstractNamedAuditedEntity} test class.
 */
public class AuditedBeanTest {

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable, Auditable)}
	 */
	@Test
	public void testCopyAuditData() {
		final Auditable<String, Integer> from = newAuditable();
		final AbstractNamedAuditedEntity<Integer> to = new AbstractNamedAuditedEntity<Integer>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};
		to.setName("two");
		AuditedBean.copyAuditData(from, to);
		assertData(to);
		Assert.assertEquals("two", to.getName());
		Assert.assertEquals(0, to.compareTo(to));
		Assert.assertTrue(to.toString().endsWith("(name=two)"));
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable, Auditable)}
	 */
	@Test
	public void testToString() {
		final AuditedBean<Serializable, Serializable> auditedBean = new AuditedBean<>();
		auditedBean.setId(0);
		Assert.assertEquals("AuditedBean(id=0)", auditedBean.toString());
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable, Auditable)}
	 */
	@Test
	public void testCopyAuditDataFromNull() {
		final Auditable<String, ?> to = newAuditable();
		AuditedBean.copyAuditData(null, to);
		assertData(to);
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable)}
	 */
	@Test
	public void testCopyAuditDataInstanceFromNull() {
		final AuditedBean<String, Integer> audited = newAuditedVo();
		audited.copyAuditData(null);
		assertData(audited);
	}

	/**
	 * Test {@link AuditedBean#getCreatedDate()} and {@link AuditedBean#getLastModifiedDate()}
	 */
	@Test
	public void testGetDateNull() {
		final Auditable<String, Integer> from = new AbstractAudited<Integer>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};
		final AuditedBean<String, Integer> audited = new AuditedBean<>();
		audited.copyAuditData(from);
		Assert.assertNull(audited.getCreatedDate());
		Assert.assertNull(audited.getLastModifiedDate());
	}

	/**
	 * Test {@link AuditedBean#copyAuditData(Auditable)}
	 */
	@Test
	public void testCopyAuditDataInstance() {
		final Auditable<String, Integer> from = newAuditable();
		final AuditedBean<String, Integer> audited = new AuditedBean<>();
		audited.copyAuditData(from);
		assertData(audited);
	}

	private AuditedBean<String, Integer> newAuditedVo() {
		final AuditedBean<String, Integer> audited = new AuditedBean<>();
		audited.setCreatedBy("any");
		audited.setLastModifiedBy("one");
		audited.setCreatedDate(new Date(0));
		audited.setLastModifiedDate(new Date(1));
		return audited;
	}

	private Auditable<String, Integer> newAuditable() {
		Auditable<String, Integer> from = new AbstractAudited<Integer>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};
		from.setCreatedBy("any");
		from.setLastModifiedBy("one");
		from.setCreatedDate(new DateTime(0));
		from.setLastModifiedDate(new DateTime(1));
		return from;
	}

	private void assertData(final Auditable<String, ?> to) {
		Assert.assertEquals("any", to.getCreatedBy());
		Assert.assertEquals("one", to.getLastModifiedBy());
		Assert.assertEquals(new DateTime(0), to.getCreatedDate());
		Assert.assertEquals(new DateTime(1), to.getLastModifiedDate());
	}

	private void assertData(final AuditedBean<String, Integer> audited) {
		Assert.assertEquals("any", audited.getCreatedBy());
		Assert.assertEquals("one", audited.getLastModifiedBy());
		Assert.assertEquals(new Date(0), audited.getCreatedDate());
		Assert.assertEquals(new Date(1), audited.getLastModifiedDate());
	}
}