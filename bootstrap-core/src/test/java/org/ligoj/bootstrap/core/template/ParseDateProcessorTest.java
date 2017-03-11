package org.ligoj.bootstrap.core.template;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

	/**
	 * Simple date format of static context.
	 */
	@Test
	public void testGetValue() {
		Assert.assertEquals(1401420659000L, new ParseDateProcessor("yyyy/MM/dd HH:mm:ss").getValue("2014/05/30 5:30:59").getTime());
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
