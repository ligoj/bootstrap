package org.ligoj.bootstrap.core.resource;


import org.junit.Assert;
import org.junit.Test;

/**
 * A business exception.
 */
public class BusinessExceptionTest {

	/**
	 * Simple constructor test.
	 */
	@Test
	public void testException() {
		final Error cause = new IllegalAccessError();
		final BusinessException exception = new BusinessException("null", cause, "param1");
		Assert.assertEquals(1, exception.getParameters().length);
		Assert.assertEquals("null" , exception.getMessage());
		Assert.assertEquals("param1" , exception.getParameters()[0]);
		Assert.assertEquals(cause, exception.getCause());
	}

	/**
	 * No cause, no parameter.
	 */
	@Test
	public void testExceptionNoCause() {
		final BusinessException exception = new BusinessException("null");
		Assert.assertEquals(0, exception.getParameters().length);
		Assert.assertEquals("null" , exception.getMessage());
		Assert.assertEquals(null, exception.getCause());
	}
}