/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import org.apache.hc.core5.http.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link SessionAuthCurlProcessor}
 */
class SessionAuthCurlProcessorTest {

	/**
	 * First request means authentication token sent.
	 */
	@Test
	void processFirstRequest() {
		final var request = new CurlRequest("", "", "");
		try (final CurlProcessor processor = new SessionAuthCurlProcessor("junit", "passwd") {
			@Override
			protected boolean call(final CurlRequest request, final String url) {
				return true;
			}
		}) {
			Assertions.assertTrue(processor.process(request));
		}
		Assertions.assertEquals("Basic anVuaXQ6cGFzc3dk", request.getHeaders().get(HttpHeaders.AUTHORIZATION));
	}

	/**
	 * Not first request means the authentication token is not sent again.
	 */
	@Test
	void process() {
		final var request = new CurlRequest("", "", "");
		request.counter = 1;
		try (final CurlProcessor processor = new SessionAuthCurlProcessor("junit", "passwd") {
			@Override
			protected boolean call(final CurlRequest request, final String url) {
				return true;
			}
		}) {
			Assertions.assertTrue(processor.process(request));
		}
		Assertions.assertTrue(request.getHeaders().isEmpty());
	}

}
