/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import javax.servlet.AsyncContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.mock.web.DelegatingServletOutputStream;

/**
 * Test class of {@link BackendProxyServlet}
 */
class BackendProxyServletTest {
	
	private static final String MAX_THREADS = "10";

	private BackendProxyServlet servlet;

	private ServletContext servletContext;

	private Callback callback;

	@BeforeEach
    void setup() throws IllegalAccessException {
		servletContext = Mockito.mock(ServletContext.class);
		servlet = new BackendProxyServlet() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				_log = Log.getLogger("junit");
			}

			@Override
			public ServletContext getServletContext() {
				return servletContext;
			}
		};

		FieldUtils.writeField(servlet, "_log", Mockito.mock(Logger.class), true);
		callback = Mockito.mock(Callback.class);
	}

	@Test
    void init() throws ServletException {
		setupRedirection("/", "/");
	}

	@Test
    void initNoEndpoint() {
		Assertions.assertThrows(UnavailableException.class, () -> setupRedirection("/", ""));
	}

	@Test
    void rewriteURINotMatch() throws ServletException {
		setupRedirection("/nomatch", "any");

		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn("/some");
		Assertions.assertNull(servlet.rewriteTarget(request));
	}

	@Test
    void rewriteURIBlacklisted() throws ServletException {
		setupRedirection("/blacklist", "http://blacklist-host:1/context");
		servlet.getBlackListHosts().add("blacklist-host:1");

		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn("/blacklist/any");
		Assertions.assertNull(servlet.rewriteTarget(request));
	}

	@Test
    void rewriteURI() throws ServletException {
		setupRedirection("/rest", "http://proxified:1/endpoint");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertEquals("http://proxified:1/endpoint/any", rewriteURI);
	}

	/**
	 * Blacklist management
	 */
	@Test
    void rewriteURIInvalidTarget() throws ServletException {
		servlet.getBlackListHosts().add("proxified:1");
		setupRedirection("/rest", "http://proxified:1/endpoint");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertNull(rewriteURI);
	}

	@Test
    void rewriteURIWithQuery() throws ServletException {
		setupRedirection("/rest", "http://proxified:1/endpoint");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getQueryString()).thenReturn("query");
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertEquals("http://proxified:1/endpoint/any?query", rewriteURI);
	}

	@Test
    void rewriteURIWithSoloApiInQuery() throws ServletException {
		setupRedirection("/rest", "http://proxified:1/endpoint");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("api-key")).thenReturn("VALUE-1-a");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getQueryString()).thenReturn("api-key=VALUE-1-a");
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertEquals("http://proxified:1/endpoint/any", rewriteURI);
	}

	@Test
    void rewriteURIWithInsertedApiInQuery() throws ServletException {
		setupRedirection("/rest", "http://proxified:1/endpoint");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("api-key")).thenReturn("VALUE-1-a");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getQueryString()).thenReturn("p=2&api-key=VALUE-1-a&q=3&r");
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertEquals("http://proxified:1/endpoint/any?p=2&q=3&r", rewriteURI);
	}

	@Test
    void rewriteURIWithInsertedApiStartQuery() throws ServletException {
		setupRedirection("/rest", "http://proxified:1/endpoint");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("api-key")).thenReturn("VALUE-1-a");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getQueryString()).thenReturn("api-key=VALUE-1-a&q=3&r");
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertEquals("http://proxified:1/endpoint/any?q=3&r", rewriteURI);
	}

	@Test
    void rewriteURIWithApiNotQuery() throws ServletException {
		setupRedirection("/rest", "http://proxified:1/endpoint");
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("api-key")).thenReturn("VALUE-1-a");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getQueryString()).thenReturn("query");
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertEquals("http://proxified:1/endpoint/any?query", rewriteURI);
	}

	private void setupRedirection(final String prefix, final String proxyTo) throws ServletException {
		final var servletConfig = Mockito.mock(ServletConfig.class);
		Mockito.when(servletConfig.getServletName()).thenReturn("a");
		System.setProperty("endpoint.rest", proxyTo);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);
		Mockito.when(servletConfig.getInitParameter("proxyToKey")).thenReturn("endpoint.rest");
		Mockito.when(servletConfig.getInitParameter("prefix")).thenReturn(prefix);
		Mockito.when(servletConfig.getInitParameter("maxThreads")).thenReturn(MAX_THREADS);
		servlet.init(servletConfig);
	}

	@Test
    void addProxyHeaders() {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var exchange = Mockito.mock(Request.class);
		final var session = Mockito.mock(HttpSession.class);
		final var principal = Mockito.mock(Principal.class);
		Mockito.when(request.getSession(false)).thenReturn(session);
		Mockito.when(session.getId()).thenReturn("J_SESSIONID");
		Mockito.when(request.getUserPrincipal()).thenReturn(principal);
		Mockito.when(principal.getName()).thenReturn("junit");
		servlet.addProxyHeaders(request, exchange);
		Mockito.verify(exchange, Mockito.times(1)).header("SM_UNIVERSALID", "junit");
		Mockito.verify(exchange, Mockito.times(1)).header("SM_SESSIONID", "J_SESSIONID");
	}

	@Test
    void addProxyHeadersCookie() {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var exchange = Mockito.mock(Request.class);
		final var session = Mockito.mock(HttpSession.class);
		final var principal = Mockito.mock(Principal.class);
		Mockito.when(request.getSession(false)).thenReturn(session);
		Mockito.when(request.getHeader("cookie")).thenReturn("JSESSIONID=value1; OTHER1=value2   ;   OTHER2=value3  ");
		Mockito.when(session.getId()).thenReturn("J_SESSIONID");
		Mockito.when(request.getUserPrincipal()).thenReturn(principal);
		Mockito.when(principal.getName()).thenReturn("junit");
		servlet.addProxyHeaders(request, exchange);
		Mockito.verify(exchange, Mockito.times(1)).header("SM_UNIVERSALID", "junit");
		Mockito.verify(exchange, Mockito.times(1)).header("SM_SESSIONID", "J_SESSIONID");
		Mockito.verify(exchange, Mockito.times(1)).header("cookie", "OTHER1=value2; OTHER2=value3");
	}

	/**
	 * Manage the session but API key.
	 */
	@Test
    void customizeExchangeNoPrincipal() throws ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var exchange = Mockito.mock(Request.class);
		Mockito.when(request.getParameter("api-key")).thenReturn("token");
		Mockito.when(request.getParameter("api-user")).thenReturn("user");
		setupRedirection("a", "a");
		servlet.addProxyHeaders(request, exchange);
		Mockito.verify(exchange, Mockito.times(1)).header("SM_UNIVERSALID", "user");
		Mockito.verify(exchange, Mockito.times(1)).header("SM_SESSIONID", null);
		Mockito.verify(exchange, Mockito.times(1)).header("x-api-key", "token");
	}

	@Test
    void onProxyResponseFailure() throws IOException, ServletException {
		init();
		final var response = Mockito.mock(HttpServletResponse.class);
		final var byteArrayOutputStream = new ByteArrayOutputStream();
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(byteArrayOutputStream));
		final var request = Mockito.mock(HttpServletRequest.class);
		final var asyncContext = Mockito.mock(AsyncContext.class);
		Mockito.when(request.getAsyncContext()).thenReturn(asyncContext);
		servlet.onProxyResponseFailure(request, response, null, new Exception());
		Mockito.verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		Assertions.assertEquals("{\"code\":\"business-down\"}", byteArrayOutputStream.toString(StandardCharsets.UTF_8.name()));
	}

	@Test
    void onProxyResponseFailureNotClosable() throws IOException, ServletException {
		init();
		final var response = Mockito.mock(HttpServletResponse.class);
		final var os = Mockito.mock(ServletOutputStream.class);
		Mockito.doThrow(new IOException()).when(os).write(ArgumentMatchers.any(byte[].class));
		Mockito.when(response.getOutputStream()).thenReturn(os);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var asyncContext = Mockito.mock(AsyncContext.class);
		Mockito.when(request.getAsyncContext()).thenReturn(asyncContext);
		servlet.onProxyResponseFailure(request, response, null, new Exception());
	}

	@Test
    void onProxyResponseFailureTimeout() throws IOException, ServletException {
		init();
		final var response = Mockito.mock(HttpServletResponse.class);
		final var byteArrayOutputStream = new ByteArrayOutputStream();
		Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(byteArrayOutputStream));
		final var request = Mockito.mock(HttpServletRequest.class);
		final var asyncContext = Mockito.mock(AsyncContext.class);
		Mockito.when(request.getAsyncContext()).thenReturn(asyncContext);
		servlet.onProxyResponseFailure(request, response, null, new TimeoutException());
		Mockito.verify(response).setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
	}

	@Test
    void onProxyResponseFailureCommitted() throws IOException, ServletException {
		init();
		final var response = Mockito.mock(HttpServletResponse.class);
		final var os = Mockito.mock(ServletOutputStream.class);
		Mockito.doThrow(new IOException()).when(os).write(ArgumentMatchers.any(byte[].class));
		Mockito.when(response.getOutputStream()).thenReturn(os);
		Mockito.when(response.isCommitted()).thenReturn(true);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var asyncContext = Mockito.mock(AsyncContext.class);
		Mockito.when(request.getAsyncContext()).thenReturn(asyncContext);
		servlet.onProxyResponseFailure(request, response, null, new Exception());
	}

	@Test
    void filterServerResponseHeaderSkipXContent() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "x-content-type-options", null));
	}

	@Test
    void filterServerResponseHeaderSkipXFrame() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "x-frame-options", null));
	}

	@Test
    void filterServerResponseHeaderSkipXXss() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "x-xss-protection", null));
	}

	@Test
    void filterServerResponseHeaderSkipPragma() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "pragma", null));
	}

	@Test
    void filterServerResponseHeaderSkipCacheControl() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "cache-control", null));
	}

	@Test
    void filterServerResponseHeaderSkipVisited() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "visited", null));
	}

	@Test
    void filterServerResponseHeaderSkipServer() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "Server", null));
	}

	@Test
    void filterServerResponseHeaderSkipExpires() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "Expires", null));
	}

	@Test
    void filterServerResponseHeaderSkipDate() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "Date", null));
	}

	@Test
    void filterServerResponseHeader() {
		Assertions.assertEquals("application/json;charset=UTF-8",
				servlet.filterServerResponseHeader(null, null, "Content-Type", "application/json;charset=UTF-8"));
	}

	@Test
    void filterServerResponseHeaderSessionID() {
		Assertions.assertNull(servlet.filterServerResponseHeader(null, null, "set-cookie", "JSESSIONID=BLOCKED"));
	}

	@Test
    void filterServerResponseHeaderOk() {
		Assertions.assertEquals("SOME=PASS", servlet.filterServerResponseHeader(null, null, "set-cookie", "SOME=PASS"));
	}

	@Test
    void isAjaxRequestXRequest() {
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		Assertions.assertTrue(BackendProxyServlet.isAjaxRequest(request));
	}

	@Test
    void isAjaxRequest() {
		final var request = Mockito.mock(HttpServletRequest.class);
		Assertions.assertFalse(BackendProxyServlet.isAjaxRequest(request));
	}

	@Test
    void getRoot() {
		final var request = Mockito.mock(HttpServletRequest.class);
		Assertions.assertEquals(request, servlet.getRoot(new HttpServletRequestWrapper(request)));
	}

	/**
	 * 404 Error for non Ajax request forward to the normal 404 page.
	 */
	@Test
    void onResponseContentForward() throws ServletException, IOException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var dispatcher = Mockito.mock(RequestDispatcher.class);
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		Mockito.when(servletContext.getRequestDispatcher("/404.html")).thenReturn(dispatcher);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0, callback);
		Mockito.verify(dispatcher, Mockito.times(1)).forward(request, response);

	}

	@Test
    void onResponseContentForwardError() throws ServletException, IOException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var dispatcher = Mockito.mock(RequestDispatcher.class);
		final var toBeThrown = new ServletException();
		Mockito.doThrow(toBeThrown).when(dispatcher).forward(request, response);
		Mockito.when(servletContext.getRequestDispatcher("/404.html")).thenReturn(dispatcher);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0, callback);
		Mockito.verify(callback, Mockito.times(1)).failed(ArgumentMatchers.any(Exception.class));
	}

	@Test
    void onResponseContent() throws IOException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		final var proxyResponse = Mockito.mock(Response.class);
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0, callback);
		Mockito.verify(outputStream, Mockito.times(1)).write(null, 0, 0);

	}

	@Test
    void onResponseContentForbiddenAjax() throws IOException {
		checkStatusForward(HttpServletResponse.SC_FORBIDDEN);
	}

	@Test
    void onResponseContentUnAuthorizedAjax() throws IOException {
		checkStatusForward(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Test
    void onResponseContentNotFoundAjax() throws IOException {
		checkStatusForward(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
    void onResponseContentServerErrorAjax() throws IOException {
		checkStatusForward(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
    void onResponseContentMethodErrorAjax() throws IOException {
		checkStatusForward(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	private void checkStatusForward(final int status) throws IOException {
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		final var response = Mockito.mock(HttpServletResponse.class);
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(status);
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0, callback);
		Mockito.verify(outputStream, Mockito.times(1)).write(null, 0, 0);
		Mockito.verify(callback, Mockito.times(1)).succeeded();
	}

	@Test
    void onResponseHeaders() {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getHeaders()).thenReturn(new HttpFields());
		servlet.onServerResponseHeaders(request, response, proxyResponse);
		Mockito.verify(response, Mockito.never()).addHeader("Content-Type", "text/html");
	}

	@Test
    void onResponseHeadersNotFoundAjax() {
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		final var response = Mockito.mock(HttpServletResponse.class);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
		Mockito.when(proxyResponse.getHeaders()).thenReturn(new HttpFields());
		servlet.onServerResponseHeaders(request, response, proxyResponse);
		Mockito.verify(response, Mockito.never()).addHeader("Content-Type", "text/html");
	}

	@Test
    void onResponseHeadersNotFound() {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
		servlet.onServerResponseHeaders(request, response, proxyResponse);
		Mockito.verify(response, Mockito.times(1)).addHeader("Content-Type", "text/html");
	}

	@Test
    void getRequiredInitParameter() {
		final var servletConfig = Mockito.mock(ServletConfig.class);

		Mockito.when(servletConfig.getServletName()).thenReturn("a");
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);
		Mockito.when(servletConfig.getInitParameter("prefix")).thenReturn("prefix");
		Mockito.when(servletConfig.getInitParameter("maxThreads")).thenReturn(MAX_THREADS);
		Assertions.assertThrows(UnavailableException.class, () -> servlet.init(servletConfig));
	}

	@Test
    void findConnectionHeaders() {
        var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeaders(HttpHeader.CONNECTION.asString())).thenReturn(Collections.emptyEnumeration());
		servlet.findConnectionHeaders(request);
	}

}
