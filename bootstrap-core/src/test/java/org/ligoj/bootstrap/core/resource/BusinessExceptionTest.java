/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A business exception.
 */
class BusinessExceptionTest {

	/**
	 * Simple constructor test.
	 */
	@Test
    void testException() {
		final Error cause = new IllegalAccessError();
		final var exception = new BusinessException("null", cause, "param1");
		Assertions.assertEquals(1, exception.getParameters().length);
		Assertions.assertEquals("null" , exception.getMessage());
		Assertions.assertEquals("param1" , exception.getParameters()[0]);
		Assertions.assertEquals(cause, exception.getCause());
	}

	/**
	 * No cause, no parameter.
	 */
	@Test
    void testExceptionNoCause() {
		final var exception = new BusinessException("null");
		Assertions.assertEquals(0, exception.getParameters().length);
		Assertions.assertEquals("null" , exception.getMessage());
		Assertions.assertNull(exception.getCause());
	}
}