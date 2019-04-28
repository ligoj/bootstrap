/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link LowerCaseValidator}
 */
class LowerCaseValidatorTest {

	@Test
	void testValidString() {
		Assertions.assertTrue(new LowerCaseValidator().isValid("any1(]=", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid("any", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid("167.:", null));
		Assertions.assertTrue(new LowerCaseValidator().isValid(null, null));
	}

	@Test
	void testInvalidString() {
		Assertions.assertFalse(new LowerCaseValidator().isValid("aNy", null));
		Assertions.assertFalse(new LowerCaseValidator().isValid("A", null));
	}
}
