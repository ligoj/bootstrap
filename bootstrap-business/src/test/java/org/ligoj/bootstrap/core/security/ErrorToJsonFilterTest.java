/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

/**
 * Test class of {@link ErrorToJsonFilter}
 */
class ErrorToJsonFilterTest {

	@Test
	void testOk() throws IOException {
		final var request = mock(ServletRequest.class);
		final var response = mock(HttpServletResponse.class);
		final var chain = mock(FilterChain.class);
		new ErrorToJsonFilter().doFilter(request, response, chain);
		verify(response, never()).setStatus(ArgumentMatchers.anyInt());
	}

	@Test
	void testKo() throws IOException, ServletException {
		final var request = mock(ServletRequest.class);
		final var response = mock(HttpServletResponse.class);
		final var chain = mock(FilterChain.class);
		doThrow(new IOException()).when(chain).doFilter(request, response);
		final var outputStream = mock(ServletOutputStream.class);
		when(response.getOutputStream()).thenReturn(outputStream);

		new ErrorToJsonFilter().doFilter(request, response, chain);
		verify(response, times(1)).setStatus(500);
		verify(response, times(1)).setContentType("application/json");
		verify(response, times(1)).setCharacterEncoding("UTF-8");
		verify(outputStream, times(1)).write("{\"code\":\"internal\"}".getBytes(StandardCharsets.UTF_8));
	}
}
