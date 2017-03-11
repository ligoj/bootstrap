package org.ligoj.bootstrap.core.template;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test class of {@link DecimalDateProcessor}
 */
public class DecimalDateProcessorTest {

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
		Assert.assertEquals(1278220350000L, new DecimalDateProcessor().getValue("40363.300347222").getTime());
	}

	/**
	 * Simple date format of dynamic context.
	 */
	@Test
	public void testGetItemValue() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Invalid string '40363-300347222' for decimal Excel date");
		new DecimalDateProcessor().getValue("40363-300347222");
	}

}
