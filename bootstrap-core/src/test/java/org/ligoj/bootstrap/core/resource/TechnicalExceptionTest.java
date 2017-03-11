package org.ligoj.bootstrap.core.resource;


import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals(1, exception.getParameters().length);
		Assert.assertEquals("null", exception.getMessage());
		Assert.assertEquals("param1", exception.getParameters()[0]);
		Assert.assertEquals(cause, exception.getCause());
	}

	/**
	 * No cause, no parameter.
	 */
	@Test
	public void testExceptionNoCause() {
		final TechnicalException exception = new TechnicalException("null");
		Assert.assertEquals(0, exception.getParameters().length);
		Assert.assertEquals("null", exception.getMessage());
		Assert.assertEquals(null, exception.getCause());
	}
}