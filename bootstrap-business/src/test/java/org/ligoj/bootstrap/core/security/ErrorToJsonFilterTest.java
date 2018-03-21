/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Test class of {@link ErrorToJsonFilter}
 */
public class ErrorToJsonFilterTest {

	@Test
	public void testOk() throws IOException {
		final ServletRequest request = Mockito.mock(ServletRequest.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		final FilterChain chain = Mockito.mock(FilterChain.class);
		new ErrorToJsonFilter().doFilter(request, response, chain);
		Mockito.verify(response, Mockito.never()).setStatus(ArgumentMatchers.anyInt());
	}

	@Test
	public void testKo() throws IOException, ServletException {
		final ServletRequest request = Mockito.mock(ServletRequest.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		final FilterChain chain = Mockito.mock(FilterChain.class);
		Mockito.doThrow(new IOException()).when(chain).doFilter(request, response);
		final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);

		new ErrorToJsonFilter().doFilter(request, response, chain);
		Mockito.verify(response, Mockito.times(1)).setStatus(500);
		Mockito.verify(response, Mockito.times(1)).setContentType("application/json");
		Mockito.verify(response, Mockito.times(1)).setCharacterEncoding("UTF-8");
		Mockito.verify(outputStream, Mockito.times(1)).write("{\"code\":\"internal\"}".getBytes(StandardCharsets.UTF_8.name()));
	}
}
