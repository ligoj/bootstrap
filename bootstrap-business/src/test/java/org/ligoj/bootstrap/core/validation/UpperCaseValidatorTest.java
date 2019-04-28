/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link UpperCaseValidator}
 */
class UpperCaseValidatorTest {

	@Test
	void testValidString() {
		Assertions.assertTrue(new UpperCaseValidator().isValid("ANY(]=", null));
		Assertions.assertTrue(new UpperCaseValidator().isValid("ANY", null));
		Assertions.assertTrue(new UpperCaseValidator().isValid("167.:", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid(null, null));
	}

	@Test
	void testInvalidString() {
		Assertions.assertFalse(new UpperCaseValidator().isValid("AnY", null));
		Assertions.assertFalse(new UpperCaseValidator().isValid("a", null));
	}
}
