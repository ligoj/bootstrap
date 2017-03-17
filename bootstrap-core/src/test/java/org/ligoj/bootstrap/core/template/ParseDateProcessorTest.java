package org.ligoj.bootstrap.core.template;

import java.util.TimeZone;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ligoj.bootstrap.core.DateUtils;

/**
 * Test class of {@link ParseDateProcessor}
 */
public class ParseDateProcessorTest {

	/**
	 * Rule manager for exception.
	 */
	@Rule
	// CHECKSTYLE:OFF -- expected by JUnit
	public ExpectedException thrown = ExpectedException.none();
	// CHECKSTYLE:ON

	@BeforeClass
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
		Assert.assertEquals(1401427859000L, new ParseDateProcessor("yyyy/MM/dd HH:mm:ss").getValue("2014/05/30 5:30:59").getTime());
	}

	/**
	 * Simple date format of dynamic context.
	 */
	@Test
	public void testGetItemValue() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Invalid string 'ABCD/05/30 5:30:59' for format 'yyyy/MM/dd HH:mm:ss'");
		new ParseDateProcessor("yyyy/MM/dd HH:mm:ss").getValue("ABCD/05/30 5:30:59");
	}

}
