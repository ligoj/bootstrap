/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link UpperCaseValidator}
 */
public class UpperCaseValidatorTest {

	@Test
	public void testValidString() {
		Assertions.assertTrue(new UpperCaseValidator().isValid("AZERTY1(]=", null));
		Assertions.assertTrue(new UpperCaseValidator().isValid("AZERTY", null));
		Assertions.assertTrue(new UpperCaseValidator().isValid("167.:", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid(null, null));
	}

	@Test
	public void testInvalidString() {
		Assertions.assertFalse(new UpperCaseValidator().isValid("AZeRTY", null));
		Assertions.assertFalse(new UpperCaseValidator().isValid("a", null));
	}
}
