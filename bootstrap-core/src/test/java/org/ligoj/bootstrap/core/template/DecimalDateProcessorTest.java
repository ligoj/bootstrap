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
public class DecimalDateProcessorTest {

	@BeforeAll
	public static void init() {
		// Fix UTC time zone for this test, since date are compared
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Simple date format of static context.
	 */
	@Test
	public void testGetValue() {
		Assertions.assertEquals(1278227550000L, new DecimalDateProcessor().getValue("40363.300347222").getTime());
	}

	/**
	 * Simple date format of dynamic context.
	 */
	@Test
	public void testGetItemValue() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DecimalDateProcessor().getValue("40363-300347222");
		}, "Invalid string '40363-300347222' for decimal Excel date");
	}

}
