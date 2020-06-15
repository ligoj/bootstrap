/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import org.apache.http.auth.AUTH;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link AuthCurlProcessor}
 */
class AuthCurlProcessorTest {

	/**
	 * Process with provided and not empty credentials.
	 */
	@Test
	void process() {
        var request = new CurlRequest("", "", "");
		try (final CurlProcessor processor = new AuthCurlProcessor("junit", "passwd") {
			@Override
			protected boolean call(final CurlRequest request, final String url) {
				return true;
			}
		}) {
			processor.process(request);
			Assertions.assertEquals("Basic anVuaXQ6cGFzc3dk", request.getHeaders().get(AUTH.WWW_AUTH_RESP));
			request = new CurlRequest("", "", "");
			processor.process(request);
			Assertions.assertEquals("Basic anVuaXQ6cGFzc3dk", request.getHeaders().get(AUTH.WWW_AUTH_RESP));
		}
	}

	/**
	 * Process without provided user.
	 */
	@Test
	void processNoUser() {
		final var request = new CurlRequest("", "", "");
		try (final CurlProcessor processor = new AuthCurlProcessor("", "any") {
			@Override
			protected boolean call(final CurlRequest request, final String url) {
				return true;
			}
		}) {
			processor.process(request);
			Assertions.assertFalse(request.getHeaders().containsKey(AUTH.WWW_AUTH_RESP));
		}
	}

}
