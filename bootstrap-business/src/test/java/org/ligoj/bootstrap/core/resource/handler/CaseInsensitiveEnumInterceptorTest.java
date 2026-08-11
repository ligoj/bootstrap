/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import org.apache.cxf.message.Message;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test class of {@link CaseInsensitiveEnumInterceptor}
 */
class CaseInsensitiveEnumInterceptorTest {

	@Test
	void handleMessage() {
        var message = mock(Message.class);
		new CaseInsensitiveEnumInterceptor().handleMessage(message);
		verify(message).put("enum.conversion.case.sensitive", Boolean.TRUE);
	}
}
