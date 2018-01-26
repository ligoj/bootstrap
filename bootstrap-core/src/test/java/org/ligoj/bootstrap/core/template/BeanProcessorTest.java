package org.ligoj.bootstrap.core.template;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
		Assertions.assertEquals("any", new BeanProcessor<>(SystemUser.class, "login").getValue(contextData));
	}

	/**
	 * Simple test of a <code>null</code> valid property.
	 */
	@Test
	public void getValueNull() {
		Assertions.assertNull(new BeanProcessor<>(SystemUser.class, "login").getValue((SystemUser) null));
	}

	/**
	 * Simple test of a invalid property name.
	 */
	@Test
	public void getValueInvalidProperty() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			new BeanProcessor<>(SystemUser.class, "_any").getClass();
		});
	}

	/**
	 * Simple test of non compatible object.
	 */
	@Test
	public void getValueInvalidBean() {
		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(3);
		Assertions.assertThrows(IllegalStateException.class, () -> {
			new BeanProcessor<>(SystemUser.class, "login").getValue(contextData);
		});
	}
}
