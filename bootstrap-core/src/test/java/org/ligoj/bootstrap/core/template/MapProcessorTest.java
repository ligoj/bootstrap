/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.template;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test class of {@link MapProcessor}
 */
class MapProcessorTest {

	/**
	 * Simple test of a valid property and a not null value.
	 */
	@Test
    void testGetValue() {
		final Deque<Object> contextData = new LinkedList<>();
		final var systemUser = new SystemUser();
		systemUser.setLogin("any");
		contextData.add(systemUser);
		final Map<String, String> map = new HashMap<>();
		map.put("any", "value");
		Assertions.assertEquals("value", new MapProcessor<>(map, SystemUser.class, "login").getValue(contextData));
	}

}
