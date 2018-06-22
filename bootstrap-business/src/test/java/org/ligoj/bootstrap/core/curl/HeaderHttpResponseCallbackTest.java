/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.curl.CurlRequest;
import org.ligoj.bootstrap.core.curl.HeaderHttpResponseCallback;
import org.mockito.Mockito;

/**
 * Test class of {@link HeaderHttpResponseCallback}
 */
public class HeaderHttpResponseCallbackTest {

	@Test
	public void onResponseNoHeader() throws Exception {
		final CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
		final StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpServletResponse.SC_OK);
		Assertions.assertFalse(new HeaderHttpResponseCallback("header").onResponse(new CurlRequest("GET", "", ""), response));
	}

	@Test
	public void onResponse() throws Exception {
		final CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
		final StatusLine statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpServletResponse.SC_OK);
		Mockito.when(response.getFirstHeader("header")).thenReturn(new BasicHeader("header", "value"));
		Assertions.assertTrue(new HeaderHttpResponseCallback("header").onResponse(new CurlRequest("GET", "", ""), response));
	}
}
