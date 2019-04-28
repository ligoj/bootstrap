/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.template;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test class of {@link BeanProcessor}
 */
class BeanProcessorTest {

	/**
	 * Simple test of a valid property.
	 */
	@Test
    void getValue() {
		final Deque<Object> contextData = new LinkedList<>();
		final var systemUser = new SystemUser();
		systemUser.setLogin("any");
		contextData.add(systemUser);
		Assertions.assertEquals("any", new BeanProcessor<>(SystemUser.class, "login").getValue(contextData));
	}

	/**
	 * Simple test of a <code>null</code> valid property.
	 */
	@Test
    void getValueNull() {
		Assertions.assertNull(new BeanProcessor<>(SystemUser.class, "login").getValue((SystemUser) null));
	}

	/**
	 * Simple test of a invalid property name.
	 */
	@Test
    void getValueInvalidProperty() {
		Assertions.assertThrows(IllegalStateException.class, () -> new BeanProcessor<>(SystemUser.class, "_any"));
	}

	/**
	 * Simple test of non compatible object.
	 */
	@Test
    void getValueInvalidBean() {
		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(3);
		Assertions.assertThrows(IllegalStateException.class, () -> new BeanProcessor<>(SystemUser.class, "login").getValue(contextData));
	}
}
