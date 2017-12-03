package org.ligoj.bootstrap.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import org.ligoj.bootstrap.core.model.AbstractDescribedAuditedEntity;
import org.ligoj.bootstrap.core.model.AbstractDescribedBusinessEntity;
import org.ligoj.bootstrap.core.model.AbstractDescribedEntity;

/**
 * {@link DescribedBean} test class.
 */
public class DescribedBeanTest {

	/**
	 * Test {@link DescribedBean#copy(IDescribableBean, IDescribableBean)}
	 */
	@Test
	public void testCopyAudited() {
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
	public void testCopyEntity() {
		final IDescribableBean<Integer> from = new AbstractDescribedAuditedEntity<Integer>() {
			// Nothing
		};
		from.setDescription("any");
		from.setName("one");
		from.setId(5);

		final IDescribableBean<Integer> to = new AbstractDescribedAuditedEntity<Integer>() {
			// Nothing
		};
		DescribedBean.copy(from, to);
		assertData(to);
	}

	/**
	 * Test {@link DescribedBean#copy(IDescribableBean, IDescribableBean)}
	 */
	@Test
	public void testCopy() {
		final IDescribableBean<Integer> from = newDescribedBean();
		final IDescribableBean<Integer> to = new DescribedBean<>();
		DescribedBean.copy(from, to);
		assertData(to);
	}

	@Test
	public void testNamedBean() {
		final NamedBean<Integer> bean = new NamedBean<>(1, "VALUE");
		Assert.assertEquals("VALUE", bean.getName());
		Assert.assertEquals(1, bean.getId().intValue());
	}

	@Test
	public void testCompareTo() {
		final Set<NamedBean<Integer>> beans = new TreeSet<>();
		beans.add(new NamedBean<>(3, "VALUE3"));
		beans.add(new NamedBean<>(1, "VALUE1"));
		beans.add(new NamedBean<>(1, "value4"));
		beans.add(new NamedBean<>(1, "value0"));
		beans.add(new NamedBean<>(2, "VALUE2"));
		final List<NamedBean<Integer>> beansList = new ArrayList<>(beans);
		Assert.assertEquals("value0", beansList.get(0).getName());
		Assert.assertEquals("VALUE1", beansList.get(1).getName());
		Assert.assertEquals("VALUE2", beansList.get(2).getName());
		Assert.assertEquals("VALUE3", beansList.get(3).getName());
		Assert.assertEquals("value4", beansList.get(4).getName());
	}

	/**
	 * Test {@link NamedBean#copy(INamableBean, INamableBean)}
	 */
	@Test
	public void testCopyBusiness() {
		final IDescribableBean<String> from = new AbstractDescribedBusinessEntity<String>() {
			// Nothing
		};
		from.setName("one");
		from.setDescription("two");
		from.setId("KEY");
		final IDescribableBean<String> to = new DescribedBean<>();
		DescribedBean.copy(from, to);
		Assert.assertEquals("one", to.getName());
		Assert.assertEquals("two", to.getDescription());
		Assert.assertEquals("KEY", to.getId());
		Assert.assertEquals(0, from.compareTo(from));
		Assert.assertEquals("NamedBean(name=one)", to.toString());
	}

	/**
	 * Test {@link DescribedBean#clone(IDescribableBean)}
	 */
	@Test
	public void testClone() {
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
	public void testCloneEntity() {
		final IDescribableBean<Integer> from = new AbstractDescribedEntity<Integer>() {
			// Nothing
		};
		from.setDescription("any");
		from.setName("one");
		from.setId(5);
		Assert.assertEquals("one", from.getName());
		Assert.assertTrue(from.toString().endsWith("(name=one)"));
		Assert.assertEquals(0, from.compareTo(from));
		assertData(DescribedBean.clone(from));
	}

	/**
	 * Test {@link DescribedBean#clone(IDescribableBean)} with <code>null</code> input.
	 */
	@Test
	public void testCloneNull() {
		Assert.assertNull(DescribedBean.clone(null));
	}

	private void assertData(final IDescribableBean<Integer> to) {
		Assert.assertEquals("any", to.getDescription());
		Assert.assertEquals("one", to.getName());
		Assert.assertTrue(to.toString().endsWith("(name=one)"));
		Assert.assertEquals(5, to.getId().intValue());
	}

	private DescribedBean<Integer> newDescribedBean() {
		final DescribedBean<Integer> from = new DescribedBean<>();
		from.setDescription("any");
		from.setName("one");
		from.setId(5);
		return from;
	}
}