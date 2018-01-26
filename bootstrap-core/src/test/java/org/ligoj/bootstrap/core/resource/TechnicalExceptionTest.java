package org.ligoj.bootstrap.core.resource;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A technical exception test.
 */
public class TechnicalExceptionTest {

	/**
	 * Simple constructor test.
	 */
	@Test
	public void testException() {
		final Error cause = new IllegalAccessError();
		final TechnicalException exception = new TechnicalException("null", cause, "param1");
		Assertions.assertEquals(1, exception.getParameters().length);
		Assertions.assertEquals("null", exception.getMessage());
		Assertions.assertEquals("param1", exception.getParameters()[0]);
		Assertions.assertEquals(cause, exception.getCause());
	}

	/**
	 * No cause, no parameter.
	 */
	@Test
	public void testExceptionNoCause() {
		final TechnicalException exception = new TechnicalException("null");
		Assertions.assertEquals(0, exception.getParameters().length);
		Assertions.assertEquals("null", exception.getMessage());
		Assertions.assertEquals(null, exception.getCause());
	}
}