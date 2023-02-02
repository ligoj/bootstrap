/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link OnlyRedirectHttpResponseCallback}
 */
class OnlyRedirectHttpResponseCallbackTest {

	@Test
	void testLocation() {
		Assertions.assertFalse(new OnlyRedirectHttpResponseCallback().acceptLocation(null));
		Assertions.assertTrue(new OnlyRedirectHttpResponseCallback().acceptLocation("/"));
	}

	@Test
	void testStatus() {
		Assertions.assertFalse(new OnlyRedirectHttpResponseCallback().acceptStatus(HttpServletResponse.SC_ACCEPTED));
		Assertions.assertTrue(new OnlyRedirectHttpResponseCallback().acceptStatus(HttpServletResponse.SC_MOVED_TEMPORARILY));
	}

	@Test
	void testResponseNotMoved() {
		final var response = Mockito.mock(CloseableHttpResponse.class);
		final var statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpServletResponse.SC_OK);
		Assertions.assertFalse(new OnlyRedirectHttpResponseCallback().acceptResponse(response));
	}

	@Test
	void testResponseNoLocation() {
		final var response = Mockito.mock(CloseableHttpResponse.class);
		final var statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpServletResponse.SC_MOVED_TEMPORARILY);
		Assertions.assertFalse(new OnlyRedirectHttpResponseCallback().acceptResponse(response));
	}

	@Test
	void testResponseEmptyLocation() {
		final var response = Mockito.mock(CloseableHttpResponse.class);
		final var statusLine = Mockito.mock(StatusLine.class);
		final var header = Mockito.mock(Header.class);
		Mockito.when(response.getFirstHeader("location")).thenReturn(header);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpServletResponse.SC_MOVED_TEMPORARILY);
		Assertions.assertFalse(new OnlyRedirectHttpResponseCallback().acceptResponse(response));
	}

	@Test
	void testResponse() {
		final var response = Mockito.mock(CloseableHttpResponse.class);
		final var statusLine = Mockito.mock(StatusLine.class);
		final var header = Mockito.mock(Header.class);
		Mockito.when(response.getFirstHeader("location")).thenReturn(header);
		Mockito.when(header.getValue()).thenReturn("/");
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpServletResponse.SC_MOVED_TEMPORARILY);
		Assertions.assertTrue(new OnlyRedirectHttpResponseCallback().acceptResponse(response));
	}
}
