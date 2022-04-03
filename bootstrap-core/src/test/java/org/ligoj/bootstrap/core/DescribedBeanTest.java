/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.model.AbstractDescribedAuditedEntity;
import org.ligoj.bootstrap.core.model.AbstractDescribedBusinessEntity;
import org.ligoj.bootstrap.core.model.AbstractDescribedEntity;

/**
 * {@link DescribedBean} test class.
 */
class DescribedBeanTest {

	/**
	 * Test {@link DescribedBean#copy(IDescribableBean, IDescribableBean)}
	 */
	@Test
    void testCopyAudited() {
		final IDescribableBean<Integer> from = new DescribedAuditedBean<>();
		from.setDescription("any");
		from.setName("one");
		from.setId(5);
		final IDescribableBean<Integer> to = new DescribedAuditedBean<>();
		DescribedBean.copy(from, to);
		assertData(to);
	}

	/**
	 * Test {@link DescribedBean#copy(IDescribableBean, IDescribableBean)}
	 */
	@Test
    void testCopyEntity() {
		final IDescribableBean<Integer> from = new AbstractDescribedAuditedEntity<>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
			// Nothing
		};
		from.setDescription("any");
		from.setName("one");
		from.setId(5);

		final IDescribableBean<Integer> to = new AbstractDescribedAuditedEntity<>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
			// Nothing
		};
		DescribedBean.copy(from, to);
		assertData(to);
	}

	/**
	 * Test {@link DescribedBean#copy(IDescribableBean, IDescribableBean)}
	 */
	@Test
    void testCopy() {
		final IDescribableBean<Integer> from = newDescribedBean();
		final IDescribableBean<Integer> to = new DescribedBean<>();
		DescribedBean.copy(from, to);
		assertData(to);
	}

	@Test
    void testNamedBean() {
		final var bean = new NamedBean<>(1, "VALUE");
		Assertions.assertEquals("VALUE", bean.getName());
		Assertions.assertEquals(1, bean.getId().intValue());
	}

	@Test
    void testCompareTo() {
		final Set<NamedBean<Integer>> beans = new TreeSet<>();
		beans.add(new NamedBean<>(3, "VALUE3"));
		beans.add(new NamedBean<>(1, "VALUE1"));
		beans.add(new NamedBean<>(1, "value4"));
		beans.add(new NamedBean<>(1, "value0"));
		beans.add(new NamedBean<>(2, "VALUE2"));
		final List<NamedBean<Integer>> beansList = new ArrayList<>(beans);
		Assertions.assertEquals("value0", beansList.get(0).getName());
		Assertions.assertEquals("VALUE1", beansList.get(1).getName());
		Assertions.assertEquals("VALUE2", beansList.get(2).getName());
		Assertions.assertEquals("VALUE3", beansList.get(3).getName());
		Assertions.assertEquals("value4", beansList.get(4).getName());
	}

	/**
	 * Test {@link NamedBean#copy(INamableBean, INamableBean)}
	 */
	@Test
    void testCopyBusiness() {
		final IDescribableBean<String> from = new AbstractDescribedBusinessEntity<>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
			// Nothing
		};
		from.setName("one");
		from.setDescription("two");
		from.setId("KEY");
		final IDescribableBean<String> to = new DescribedBean<>();
		DescribedBean.copy(from, to);
		Assertions.assertEquals("one", to.getName());
		Assertions.assertEquals("two", to.getDescription());
		Assertions.assertEquals("KEY", to.getId());
		Assertions.assertEquals(0, from.compareTo(from));
		Assertions.assertEquals("NamedBean(name=one)", to.toString());
	}

	/**
	 * Test {@link DescribedBean#clone(IDescribableBean)}
	 */
	@Test
    void testClone() {
		final IDescribableBean<Integer> from = newDescribedBean();
		from.setDescription("any");
		from.setName("one");
		from.setId(5);
		assertData(DescribedBean.clone(from));
	}

	/**
	 * Test {@link DescribedBean#clone(IDescribableBean)}
	 */
	@Test
    void testCloneEntity() {
		final IDescribableBean<Integer> from = new AbstractDescribedEntity<>() {

			/**
			 * SID
			 */
			private static final long serialVersionUID = 1L;
			// Nothing
		};
		from.setDescription("any");
		from.setName("one");
		from.setId(5);
		Assertions.assertEquals("one", from.getName());
		Assertions.assertTrue(from.toString().endsWith("(name=one)"));
		Assertions.assertEquals(0, from.compareTo(from));
		assertData(DescribedBean.clone(from));
	}

	/**
	 * Test {@link DescribedBean#clone(IDescribableBean)} with <code>null</code> input.
	 */
	@Test
    void testCloneNull() {
		Assertions.assertNull(DescribedBean.clone(null));
	}

	private void assertData(final IDescribableBean<Integer> to) {
		Assertions.assertEquals("any", to.getDescription());
		Assertions.assertEquals("one", to.getName());
		Assertions.assertTrue(to.toString().endsWith("(name=one)"));
		Assertions.assertEquals(5, to.getId().intValue());
	}

	private DescribedBean<Integer> newDescribedBean() {
		final var from = new DescribedBean<Integer>();
		from.setDescription("any");
		from.setName("one");
		from.setId(5);
		return from;
	}
}