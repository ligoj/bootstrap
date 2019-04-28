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
class InsensitiveEnumParameterHandlerTest {

	@Test
	void testNullEnum() {
		Assertions.assertNull(new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).fromString(null));
	}

	@Test
	void testNotEnu() {
		Assertions.assertNull(new InsensitiveEnumParameterHandler().getConverter(String.class, null, null));
	}

	@Test
	void testExactMatch() {
		Assertions.assertEquals(Status.OK, new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).fromString("OK"));
	}

	@Test
	void testLowerCase() {
		Assertions.assertEquals(Status.OK, new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).fromString("ok"));
	}

	@Test
	void testToString() {
		Assertions.assertEquals("ok", new InsensitiveEnumParameterHandler().getConverter(Status.class, null, null).toString(Status.OK));
	}
}
