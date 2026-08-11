/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class of {@link HeaderHttpResponseCallback}
 */
class HeaderHttpResponseCallbackTest {

	@Test
	void onResponseNoHeader() throws Exception {
		final var response = mock(CloseableHttpResponse.class);
		when(response.getCode()).thenReturn(HttpServletResponse.SC_OK);
		Assertions.assertFalse(new HeaderHttpResponseCallback("header").onResponse(new CurlRequest("GET", "", ""), response));
	}

	@Test
	void onResponse() throws Exception {
		final var response = mock(CloseableHttpResponse.class);
		when(response.getCode()).thenReturn(HttpServletResponse.SC_OK);
		when(response.getFirstHeader("header")).thenReturn(new BasicHeader("header", "value"));
		Assertions.assertTrue(new HeaderHttpResponseCallback("header").onResponse(new CurlRequest("GET", "", ""), response));
	}
}
