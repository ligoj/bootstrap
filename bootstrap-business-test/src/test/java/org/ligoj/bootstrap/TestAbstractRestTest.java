/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Test of {@link AbstractRestTest}
 */
class TestAbstractRestTest extends AbstractRestTest {

	@Test
	void testStartRestServer2() throws IOException {
		retries = 0;
		httpclient = Mockito.mock(CloseableHttpClient.class);
		final var response = Mockito.mock(CloseableHttpResponse.class);
		final var statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class))).thenReturn(response);
		final var entity = Mockito.mock(HttpEntity.class);
		final var content = Mockito.mock(InputStream.class);
		Mockito.when(response.getEntity()).thenReturn(entity);
		Mockito.when(entity.getContent()).thenReturn(content);

		startRestServer("log4j2.json");
	}

	@Test
	void testStartRestServerKo1() throws IOException {
		retries = 1;
		httpclient = Mockito.mock(CloseableHttpClient.class);
		final var response = Mockito.mock(CloseableHttpResponse.class);
		final var statusLine = Mockito.mock(StatusLine.class);
		Mockito.when(response.getStatusLine()).thenReturn(statusLine);
		Mockito.when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_GATEWAY_TIMEOUT);
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class))).thenReturn(response);
		final var entity = Mockito.mock(HttpEntity.class);
		final var content = Mockito.mock(InputStream.class);
		Mockito.when(response.getEntity()).thenReturn(entity);
		Mockito.when(entity.getContent()).thenReturn(content);

		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}

	@Test
	void testStartRestServerKo2() throws IOException {
		retries = 0;
		httpclient = Mockito.mock(CloseableHttpClient.class);
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class))).thenThrow(new IOException());
		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}

	@Test
	void testStartRestServerKo3() throws IOException {
		retries = 0;
		httpclient = Mockito.mock(CloseableHttpClient.class);
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class)))
				.thenThrow(new HttpHostConnectException(null, null, new InetAddress[0]));
		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}
}
