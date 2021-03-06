/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import org.apache.cxf.message.Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link CaseInsensitiveEnumInterceptor}
 */
class CaseInsensitiveEnumInterceptorTest {

	@Test
	void handleMessage() {
        var message = Mockito.mock(Message.class);
		new CaseInsensitiveEnumInterceptor().handleMessage(message);
		Mockito.verify(message).put("enum.conversion.case.sensitive", Boolean.TRUE);
	}
}
