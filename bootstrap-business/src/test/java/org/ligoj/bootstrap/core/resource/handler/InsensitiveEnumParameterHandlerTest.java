/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link InsensitiveEnumParameterHandler}
 */
public class InsensitiveEnumParameterHandlerTest {

	@Test
	public void testNullEnum() {
		Assertions.assertNull(new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).fromString(null));
	}

	@Test
	public void testNotEnu() {
		Assertions.assertEquals(null, new InsensitiveEnumParameterHandler().getConverter(String.class, null, null));
	}

	@Test
	public void testExactMatch() {
		Assertions.assertEquals(Status.OK, new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).fromString("OK"));
	}

	@Test
	public void testLowerCase() {
		Assertions.assertEquals(Status.OK, new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).fromString("ok"));
	}

	@Test
	public void testToString() {
		Assertions.assertEquals("ok", new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).toString(Status.OK));
	}
}
