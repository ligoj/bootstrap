/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;

/**
 * Test of {@link AbstractRestTest}
 */
class TestAbstractRestTest extends AbstractRestTest {

	@Test
	@SuppressWarnings("unchecked")
	void testStartRestServer2() throws IOException {
		retries = 0;
		httpclient = Mockito.mock(CloseableHttpClient.class);
		final var response = Mockito.mock(CloseableHttpResponse.class);
		Mockito.when(response.getCode()).thenReturn(HttpStatus.SC_OK);
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.any(HttpClientResponseHandler.class))).thenAnswer(
				invocation -> {
					return ((HttpClientResponseHandler<CloseableHttpResponse>) invocation.getArgument(1)).handleResponse(response);
				}
		);
		final var entity = Mockito.mock(HttpEntity.class);
		final var content = Mockito.mock(InputStream.class);
		Mockito.when(response.getEntity()).thenReturn(entity);
		Mockito.when(entity.getContent()).thenReturn(content);
		Assertions.assertNotNull(startRestServer("log4j2.json"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void testStartRestServerKo1() throws IOException {
		retries = 1;
		httpclient = Mockito.mock(CloseableHttpClient.class);
		final var response = Mockito.mock(CloseableHttpResponse.class);
		Mockito.when(response.getCode()).thenReturn(HttpStatus.SC_GATEWAY_TIMEOUT);
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.any(HttpClientResponseHandler.class))).thenAnswer(
				invocation -> {
					return ((HttpClientResponseHandler<CloseableHttpResponse>) invocation.getArgument(1)).handleResponse(response);
				}
		);
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
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.any(HttpClientResponseHandler.class))).thenThrow(new IOException());
		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}

	@Test
	void testStartRestServerKo3() throws IOException {
		retries = 0;
		httpclient = Mockito.mock(CloseableHttpClient.class);
		Mockito.when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.any(HttpClientResponseHandler.class))).thenThrow(new HttpHostConnectException(""));
		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}
}
