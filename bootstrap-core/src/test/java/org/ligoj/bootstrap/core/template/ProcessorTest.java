/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.template;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test class of {@link Processor}
 */
public class ProcessorTest {

	/**
	 * Simple test of null valued data.
	 */
	@Test
	public void testGetNullValue() {
		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(3);
		Assertions.assertEquals(Integer.valueOf(3), new Processor<>().getValue(contextData));
	}

	/**
	 * Simple test of not null valued data.
	 */
	@Test
	public void testGetValue() {
		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(4);
		Assertions.assertEquals(Integer.valueOf(3), new Processor<>(3).getValue(contextData));
	}

	/**
	 * Simple wrapping test of not null valued data.
	 */
	@Test
	public void testGetWrappedValue() {
		final SystemUser systemUser = new SystemUser();
		systemUser.setLogin("any");

		final SystemRoleAssignment roleAssignment = new SystemRoleAssignment();
		roleAssignment.setUser(systemUser);

		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(roleAssignment);
		Assertions.assertEquals("any",
				new BeanProcessor<>(SystemUser.class, "login", new BeanProcessor<>(SystemRoleAssignment.class, "user")).getValue(contextData));
	}

	/**
	 * Simple wrapping test of not null valued data.
	 */
	@Test
	public void testGetWrappedSimpleValue() {
		Assertions.assertEquals(Integer.valueOf(8), new Processor<>(new Processor<>(2)).getValue(8));
	}
}
