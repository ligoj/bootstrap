package org.ligoj.bootstrap.core.template;

import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.DateUtils;

/**
 * Test class of {@link ParseDateProcessor}
 */
public class ParseDateProcessorTest {

	@BeforeAll
	public static void init() {
		System.setProperty("app.crypto.file", "src/test/resources/security.key");

		// Fix UTC time zone for this test, since date are compared
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Simple date format of static context.
	 */
	@Test
	public void testGetValue() {
		Assertions.assertEquals(1401427859000L, new ParseDateProcessor("yyyy/MM/dd HH:mm:ss").getValue("2014/05/30 5:30:59").getTime());
	}

	/**
	 * Simple date format of dynamic context.
	 */
	@Test
	public void testGetItemValue() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new ParseDateProcessor("yyyy/MM/dd HH:mm:ss").getValue("ABCD/05/30 5:30:59");
		},"Invalid string 'ABCD/05/30 5:30:59' for format 'yyyy/MM/dd HH:mm:ss'");
	}

}
