/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link LowerCaseValidator}
 */
public class LowerCaseValidatorTest {

	@Test
	public void testValidString() {
		Assertions.assertTrue(new LowerCaseValidator().isValid("azerty1(]=", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid("azerty", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid("167.:", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid(null, null));
	}

	@Test
	public void testInvalidString() {
		Assertions.assertFalse(new LowerCaseValidator().isValid("azErty", null));
		Assertions.assertFalse(new LowerCaseValidator().isValid("A", null));
	}
}
