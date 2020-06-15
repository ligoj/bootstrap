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
 * Test class of {@link ParseDateProcessor}
 */
class ParseDateProcessorTest {

	private ParseDateProcessor processor = new ParseDateProcessor("yyyy/MM/dd HH:mm:ss");

	@BeforeAll
	static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");

		// Fix UTC time zone for this test, since date are compared
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Simple date format of static context.
	 */
	@Test
	void testGetValue() {
		Assertions.assertEquals(1401427859000L, processor.getValue("2014/05/30 5:30:59").getTime());
	}

	/**
	 * Simple date format of dynamic context.
	 */
	@Test
	void testGetItemValue() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> processor.getValue("ABCD/05/30 5:30:59"),
				"Invalid string 'ABCD/05/30 5:30:59' for format 'yyyy/MM/dd HH:mm:ss'");
	}

}
