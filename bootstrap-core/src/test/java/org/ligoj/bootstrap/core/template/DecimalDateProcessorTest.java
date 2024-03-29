/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.template;

import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.DateUtils;

/**
 * Test class of {@link DecimalDateProcessor}
 */
class DecimalDateProcessorTest {

	private final DecimalDateProcessor processor = new DecimalDateProcessor();

	@BeforeAll
	static void init() {
		// Fix UTC time zone for this test, since date are compared
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Simple date format of static context.
	 */
	@Test
	void testGetValue() {
		Assertions.assertEquals(1278227550000L, processor.getValue("40363.300347222").getTime());
	}

	/**
	 * Simple date format of dynamic context.
	 */
	@Test
	void testGetItemValue() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> processor.getValue("40363-300347222"),
				"Invalid string '40363-300347222' for decimal Excel date");
	}

}
