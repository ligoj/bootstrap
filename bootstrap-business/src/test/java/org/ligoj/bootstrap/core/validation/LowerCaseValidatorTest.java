package org.ligoj.bootstrap.core.validation;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class of {@link LowerCaseValidator}
 */
public class LowerCaseValidatorTest {

	@Test
	public void testValidString() {
		Assert.assertTrue(new LowerCaseValidator().isValid("azerty1(]=", null));
		Assert.assertTrue(new LowerCaseValidator().isValid("azerty", null));
		Assert.assertTrue(new LowerCaseValidator().isValid("167.:", null));
		Assert.assertTrue(new LowerCaseValidator().isValid(null, null));
	}

	@Test
	public void testInvalidString() {
		Assert.assertFalse(new LowerCaseValidator().isValid("azErty", null));
		Assert.assertFalse(new LowerCaseValidator().isValid("A", null));
	}
}
