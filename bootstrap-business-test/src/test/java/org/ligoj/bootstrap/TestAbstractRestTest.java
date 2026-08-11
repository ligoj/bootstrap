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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test of {@link AbstractRestTest}
 */
class TestAbstractRestTest extends AbstractRestTest {

	@Test
	@SuppressWarnings("unchecked")
	void testStartRestServer2() throws IOException {
		retries = 0;
		httpclient = mock(CloseableHttpClient.class);
		final var response = mock(CloseableHttpResponse.class);
		when(response.getCode()).thenReturn(HttpStatus.SC_OK);
		when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.<HttpClientResponseHandler<CloseableHttpResponse>>any())).thenAnswer(
				invocation -> ((HttpClientResponseHandler<CloseableHttpResponse>) invocation.getArgument(1)).handleResponse(response)
		);
		final var entity = mock(HttpEntity.class);
		when(response.getEntity()).thenReturn(entity);
		when(entity.getContent()).thenAnswer(var1 -> new ByteArrayInputStream("response".getBytes(StandardCharsets.UTF_8)));
		Assertions.assertNotNull(startRestServer(null));

		final var httpGet = new HttpGet(BASE_URI + "/null");
		Assertions.assertEquals("response", execute(httpGet));
	}

	@Test
	@SuppressWarnings("unchecked")
	void testStartRestServerKo1() throws IOException {
		retries = 1;
		httpclient = mock(CloseableHttpClient.class);
		final var response = mock(CloseableHttpResponse.class);
		when(response.getCode()).thenReturn(HttpStatus.SC_GATEWAY_TIMEOUT);
		when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.<HttpClientResponseHandler<CloseableHttpResponse>>any())).thenAnswer(
				invocation -> ((HttpClientResponseHandler<CloseableHttpResponse>) invocation.getArgument(1)).handleResponse(response)
		);
		final var entity = mock(HttpEntity.class);
		final var content = mock(InputStream.class);
		when(response.getEntity()).thenReturn(entity);
		when(entity.getContent()).thenReturn(content);

		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}

	@Test
	void testStartRestServerKo2() throws IOException {
		retries = 0;
		httpclient = mock(CloseableHttpClient.class);
		when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.<HttpClientResponseHandler<CloseableHttpResponse>>any())).thenThrow(new IOException());
		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}

	@Test
	void testStartRestServerKo3() throws IOException {
		retries = 0;
		httpclient = mock(CloseableHttpClient.class);
		when(httpclient.execute(ArgumentMatchers.any(HttpGet.class), ArgumentMatchers.<HttpClientResponseHandler<CloseableHttpResponse>>any())).thenThrow(new HttpHostConnectException(""));
		Assertions.assertThrows(IllegalStateException.class, () -> startRestServer("log4j2.json"));
	}

}
