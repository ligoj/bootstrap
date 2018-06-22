/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import org.apache.http.auth.AUTH;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.curl.CurlProcessor;
import org.ligoj.bootstrap.core.curl.CurlRequest;
import org.ligoj.bootstrap.core.curl.SessionAuthCurlProcessor;

/**
 * Test class of {@link SessionAuthCurlProcessor}
 */
public class SessionAuthCurlProcessorTest {

	/**
	 * First request means authentication token sent.
	 */
	@Test
	public void processFirstRequest() {
		final CurlRequest request = new CurlRequest("", "", "");
		try (final CurlProcessor processor = new SessionAuthCurlProcessor("junit", "passwd") {
			@Override
			protected boolean call(final CurlRequest request, final String url) {
				return true;
			}
		}) {
			Assertions.assertTrue(processor.process(request));
		}
		Assertions.assertEquals("Basic anVuaXQ6cGFzc3dk", request.getHeaders().get(AUTH.WWW_AUTH_RESP));
	}

	/**
	 * Not first request means the authentication token is not sent again.
	 */
	@Test
	public void process() {
		final CurlRequest request = new CurlRequest("", "", "");
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
