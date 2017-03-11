package org.ligoj.bootstrap.core.template;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test class of {@link BeanProcessor}
 */
public class BeanProcessorTest {

	/**
	 * Simple test of a valid property.
	 */
	@Test
	public void getValue() {
		final Deque<Object> contextData = new LinkedList<>();
		final SystemUser systemUser = new SystemUser();
		systemUser.setLogin("any");
		contextData.add(systemUser);
		Assert.assertEquals("any", new BeanProcessor<>(SystemUser.class, "login").getValue(contextData));
	}

	/**
	 * Simple test of a <code>null</code> valid property.
	 */
	@Test
	public void getValueNull() {
		Assert.assertNull("any", new BeanProcessor<>(SystemUser.class, "login").getValue((SystemUser) null));
	}

	/**
	 * Simple test of a invalid property name.
	 */
	@Test(expected = IllegalStateException.class)
	public void getValueInvalidProperty() {
		new BeanProcessor<>(SystemUser.class, "_any").getClass();
	}

	/**
	 * Simple test of non compatible object.
	 */
	@Test(expected = IllegalStateException.class)
	public void getValueInvalidBean() {
		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(3);
		Assert.assertEquals(new Integer(3), new BeanProcessor<>(SystemUser.class, "login").getValue(contextData));
	}
}
