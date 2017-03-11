package org.ligoj.bootstrap.core.template;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test class of {@link MapProcessor}
 */
public class MapProcessorTest {

	/**
	 * Simple test of a valid property and a not null value.
	 */
	@Test
	public void testGetValue() {
		final Deque<Object> contextData = new LinkedList<>();
		final SystemUser systemUser = new SystemUser();
		systemUser.setLogin("any");
		contextData.add(systemUser);
		final Map<String, String> map = new HashMap<>();
		map.put("any", "value");
		Assert.assertEquals("value", new MapProcessor<>(map, SystemUser.class, "login").getValue(contextData));
	}

}
