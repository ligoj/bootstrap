package org.ligoj.bootstrap.core.validation;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class of {@link UpperCaseValidator}
 */
public class UpperCaseValidatorTest {

	@Test
	public void testValidString() {
		Assert.assertTrue(new UpperCaseValidator().isValid("AZERTY1(]=", null));
		Assert.assertTrue(new UpperCaseValidator().isValid("AZERTY", null));
		Assert.assertTrue(new UpperCaseValidator().isValid("167.:", null));
		Assert.assertTrue(new LowerCaseValidator().isValid(null, null));
	}

	@Test
	public void testInvalidString() {
		Assert.assertFalse(new UpperCaseValidator().isValid("AZeRTY", null));
		Assert.assertFalse(new UpperCaseValidator().isValid("a", null));
	}
}
