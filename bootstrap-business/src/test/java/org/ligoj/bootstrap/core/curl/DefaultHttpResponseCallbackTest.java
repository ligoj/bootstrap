/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;

/**
 * Test class of {@link DefaultHttpResponseCallback}
 */
class DefaultHttpResponseCallbackTest {

	@Test
	void onResponse() throws Exception {
		final var response = Mockito.mock(CloseableHttpResponse.class);
		Mockito.when(response.getEntity()).thenReturn(null);
		Assertions.assertTrue(new DefaultHttpResponseCallback().onResponse(new CurlRequest("GET", "", ""), response));
	}

	@Test
	void onResponseIOE() throws Exception {
		final var response = Mockito.mock(CloseableHttpResponse.class);
		final var entity = Mockito.mock(HttpEntity.class);
		final var input = Mockito.mock(InputStream.class);
		Mockito.when(response.getEntity()).thenReturn(entity);
		Mockito.when(input.read()).thenThrow(new IOException());
		final var request = new CurlRequest("GET", "", "");
		request.setSaveResponse(true);
		Mockito.when(entity.getContent()).thenReturn(input);
		Mockito.when(response.getCode()).thenReturn(HttpServletResponse.SC_OK);
		Assertions.assertFalse(new DefaultHttpResponseCallback().onResponse(request, response));
	}
}
