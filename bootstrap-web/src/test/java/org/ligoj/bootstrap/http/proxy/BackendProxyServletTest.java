/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.proxy;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.DelegatingServletOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

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
				_log = LoggerFactory.getLogger("junit");
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
	void rewriteURIInvalidUri() throws ServletException {
		servlet.getBlackListHosts().add("proxified:1");
		setupRedirection("/rest", ":invalid:uri");
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

	private void rewriteURI(final String proxy, final String query, final String rewrite) throws ServletException {
		setupRedirection("/rest", proxy);
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("api-key")).thenReturn("api-key=VALUE-1-a");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getQueryString()).thenReturn(query);
		final var rewriteURI = servlet.rewriteTarget(request);
		Assertions.assertEquals(rewrite, rewriteURI);
	}

	@Test
	void rewriteURIWithSoloApiInQuery() throws ServletException {
		rewriteURI("http://proxified:1/endpoint", "api-key=VALUE-1-a", "http://proxified:1/endpoint/any");
	}

	@Test
	void rewriteURIWithInsertedApiInQuery() throws ServletException {
		rewriteURI("http://proxified:1/endpoint", "p=2&api-key=VALUE-1-a&q=3&r",
				"http://proxified:1/endpoint/any?p=2&q=3&r");
	}

	@Test
	void rewriteURIWithInsertedApiStartQuery() throws ServletException {
		rewriteURI("http://proxified:1/endpoint", "api-key=VALUE-1-a&q=3&r", "http://proxified:1/endpoint/any?q=3&r");
	}

	@Test
	void rewriteURIWithApiNotQuery() throws ServletException {
		rewriteURI("http://proxified:1/endpoint", "query", "http://proxified:1/endpoint/any?query");
	}

	private void setupRedirection(final String prefix, final String proxyTo) throws ServletException {
		final var servletConfig = Mockito.mock(ServletConfig.class);
		Mockito.when(servletConfig.getServletName()).thenReturn("a");
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(servletConfig.getServletContext()).thenReturn(servletContext);
		Mockito.when(servletConfig.getInitParameter("proxyTo")).thenReturn(proxyTo);
		Mockito.when(servletConfig.getInitParameter("prefix")).thenReturn(prefix);
		Mockito.when(servletConfig.getInitParameter("maxThreads")).thenReturn(MAX_THREADS);
		Mockito.when(servletConfig.getInitParameter("idleTimeout")).thenReturn("120000");
		Mockito.when(servletConfig.getInitParameter("timeout")).thenReturn("0");
		Mockito.when(servletConfig.getInitParameter("apiKeyParameter")).thenReturn("api-key");
		Mockito.when(servletConfig.getInitParameter("apiKeyHeader")).thenReturn("x-api-key");
		Mockito.when(servletConfig.getInitParameter("apiUserParameter")).thenReturn("api-user");
		Mockito.when(servletConfig.getInitParameter("apiUserHeader")).thenReturn("x-api-user");
		Mockito.when(servletConfig.getInitParameter("responseBufferSize")).thenReturn(String.valueOf(16 * 1024));
		Mockito.when(servletConfig.getInitParameter("requestBufferSize")).thenReturn(String.valueOf(4 * 1024));
		Mockito.when(servletConfig.getInitParameter("maxConnections")).thenReturn("512");
		Mockito.when(servletConfig.getInitParameter("cors-origin")).thenReturn("*");
		Mockito.when(servletConfig.getInitParameter("cors-vary")).thenReturn("Origin");
		servlet.init(servletConfig);
	}

	@Test
	void addProxyHeaders() {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var headers = new HashMap<String, Object>();
		final var exchange = setupRequest(request, headers);
		final var session = Mockito.mock(HttpSession.class);
		final var principal = Mockito.mock(Principal.class);
		Mockito.when(request.getSession(false)).thenReturn(session);
		Mockito.when(session.getId()).thenReturn("J_SESSIONID");
		Mockito.when(request.getUserPrincipal()).thenReturn(principal);
		Mockito.when(principal.getName()).thenReturn("junit");
		servlet.addProxyHeaders(request, exchange);
		Assertions.assertEquals("junit", headers.get("SM_UNIVERSALID"));
		Assertions.assertEquals("J_SESSIONID", headers.get("SM_SESSIONID"));
	}

	/**
	 * Manage the session
	 */
	@Test
	void addProxyHeadersCookie() {
		final var session = Mockito.mock(HttpSession.class);
		final var principal = Mockito.mock(Principal.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var headers = new HashMap<String, Object>();
		final var proxyRequest = setupRequest(request, headers);
		Mockito.when(request.getSession(false)).thenReturn(session);
		Mockito.when(request.getHeader("cookie")).thenReturn("JSESSIONID=value1; OTHER1=value2   ;   OTHER2=value3  ");
		Mockito.when(session.getId()).thenReturn("J_SESSIONID");
		Mockito.when(request.getUserPrincipal()).thenReturn(principal);
		Mockito.when(principal.getName()).thenReturn("junit");
		servlet.addProxyHeaders(request, proxyRequest);
		Assertions.assertEquals("junit", headers.get("SM_UNIVERSALID"));
		Assertions.assertEquals("J_SESSIONID", headers.get("SM_SESSIONID"));
		Assertions.assertEquals("OTHER1=value2; OTHER2=value3", headers.get("cookie"));
	}

	/**
	 * Manage the API key (parameter) without session
	 */
	@Test
	void addProxyHeadersApiParameters() throws ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var headers = new HashMap<String, Object>();
		final var exchange = setupRequest(request, headers);
		Mockito.when(request.getParameter("api-key")).thenReturn("token");
		Mockito.when(request.getParameter("api-user")).thenReturn("user");
		setupRedirection("a", "a");
		servlet.addProxyHeaders(request, exchange);
		Assertions.assertEquals("user", headers.get("SM_UNIVERSALID"));
		Assertions.assertNull(headers.get("SM_SESSIONID"));
		Assertions.assertEquals("token", headers.get("x-api-key"));
	}

	/**
	 * Manage the API key (header) without session
	 */
	@Test
	void addProxyHeadersApiHeaders() throws ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var headers = new HashMap<String, Object>();
		final var exchange = setupRequest(request, headers);
		Mockito.when(request.getHeader("x-api-key")).thenReturn("token");
		Mockito.when(request.getHeader("x-api-user")).thenReturn("user");
		setupRedirection("a", "a");
		servlet.addProxyHeaders(request, exchange);
		Assertions.assertEquals("user", headers.get("SM_UNIVERSALID"));
		Assertions.assertNull(headers.get("SM_SESSIONID"));
		Assertions.assertEquals("token", headers.get("x-api-key"));
	}

	/**
	 * Manage the API key (header) without session
	 */
	@Test
	void addProxyHeadersAnonymous() throws ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var headers = new HashMap<String, Object>();
		final var exchange = setupRequest(request, headers);

		setupRedirection("a", "a");
		servlet.addProxyHeaders(request, exchange);
		Assertions.assertNull(headers.get("SM_UNIVERSALID"));
		Assertions.assertNull(headers.get("SM_SESSIONID"));
		Assertions.assertNull(headers.get("x-api-key"));
	}

	@SuppressWarnings("unchecked")
	private Request setupRequest(final HttpServletRequest request, final Map<String, Object> headers) {
		final var exchange = Mockito.mock(Request.class);
		final Map<String, Object> attributes = Map.of("org.eclipse.jetty.proxy.clientRequest", request);
		Mockito.when(exchange.getAttributes()).thenReturn(attributes);
		Mockito.when(exchange.getHeaders()).thenReturn(HttpFields.build());
		final HttpFields.Mutable mHeaders = Mockito.mock(HttpFields.Mutable.class);
		Mockito.when(mHeaders.add(Mockito.anyString(), Mockito.anyString())).thenAnswer(invocation -> {
			final var name = (String) invocation.getArgument(0);
			final var value = (String) invocation.getArgument(1);
			headers.put(name, value);
			return mHeaders;
		});

		Mockito.when(exchange.headers(Mockito.any(Consumer.class))).thenAnswer(invocation -> {
			((Consumer<HttpFields.Mutable>) invocation.getArgument(0)).accept(mHeaders);
			return null;
		});
		Mockito.when(request.getProtocol()).thenReturn("HTTP/1.1");
		return exchange;
	}

	/**
	 * Manage the API user without API user and session
	 */
	@Test
	void addProxyHeadersApiPartial1Headers() throws ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var headers = new HashMap<String, Object>();
		final var exchange = setupRequest(request, headers);
		Mockito.when(request.getHeader("x-api-user")).thenReturn("user");
		setupRedirection("a", "a");
		servlet.addProxyHeaders(request, exchange);
		Assertions.assertNull(headers.get("SM_UNIVERSALID"));
		Assertions.assertNull(headers.get("SM_SESSIONID"));
		Assertions.assertNull(headers.get("x-api-key"));
	}

	/**
	 * Manage the API key without API and without session
	 */
	@Test
	void addProxyHeadersApiPartial2Headers() throws ServletException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var headers = new HashMap<String, Object>();
		final var exchange = setupRequest(request, headers);
		Mockito.when(request.getHeader("x-api-key")).thenReturn("token");
		setupRedirection("a", "a");
		servlet.addProxyHeaders(request, exchange);
		Assertions.assertNull(headers.get("SM_UNIVERSALID"));
		Assertions.assertNull(headers.get("SM_SESSIONID"));
		Assertions.assertNull(headers.get("x-api-key"));
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
		Assertions.assertEquals("{\"code\":\"business-down\"}",
				byteArrayOutputStream.toString(StandardCharsets.UTF_8));
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
	void isApiRequestXRequest() {
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		Assertions.assertTrue(BackendProxyServlet.isApiRequest(request));
	}

	@Test
	void isApiRequestFromBrowser() {
		final var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
		Mockito.when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac)");
		Assertions.assertTrue(BackendProxyServlet.isApiRequest(request));
	}

	@Test
	void isApiRequest() {
		final var request = Mockito.mock(HttpServletRequest.class);
		Assertions.assertTrue(BackendProxyServlet.isApiRequest(request));
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
		Mockito.when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac)");
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		Mockito.when(servletContext.getRequestDispatcher("/404.html")).thenReturn(dispatcher);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0,
				callback);
		Mockito.verify(dispatcher, Mockito.times(1)).forward(request, response);

	}

	@Test
	void onResponseContentForwardError() throws ServletException, IOException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var dispatcher = Mockito.mock(RequestDispatcher.class);
		final var toBeThrown = new ServletException();
		Mockito.when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac)");
		Mockito.doThrow(toBeThrown).when(dispatcher).forward(request, response);
		Mockito.when(servletContext.getRequestDispatcher("/404.html")).thenReturn(dispatcher);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0,
				callback);
		Mockito.verify(callback, Mockito.times(1)).failed(ArgumentMatchers.any(Exception.class));
	}

	@Test
	void onResponseContent() throws IOException {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		final var proxyResponse = Mockito.mock(Response.class);
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0,
				callback);
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
		servlet.onResponseContent(new HttpServletRequestWrapper(request), response, proxyResponse, null, 0, 0,
				callback);
		Mockito.verify(outputStream, Mockito.times(1)).write(null, 0, 0);
		Mockito.verify(callback, Mockito.times(1)).succeeded();
	}

	@Test
	void onResponseHeaders() {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getHeaders()).thenReturn(HttpFields.build());
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
		Mockito.when(proxyResponse.getHeaders()).thenReturn(HttpFields.build());
		servlet.onServerResponseHeaders(request, response, proxyResponse);
		Mockito.verify(response, Mockito.never()).addHeader("Content-Type", "text/html");
	}

	@Test
	void onResponseHeadersNotFound() {
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		final var proxyResponse = Mockito.mock(Response.class);
		Mockito.when(proxyResponse.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);
		Mockito.when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac)");
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
	void sendProxyRequest() {
		final var clientRequest = Mockito.mock(HttpServletRequest.class);
		final var proxyResponse = Mockito.mock(HttpServletResponse.class);
		final var proxyRequest = Mockito.mock(Request.class);
		servlet.sendProxyRequest(clientRequest, proxyResponse, proxyRequest);
		Mockito.verify(proxyRequest).send(ArgumentMatchers.any(Response.CompleteListener.class));
	}

	@Test
	void findConnectionHeaders() {
		var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeaders(HttpHeader.CONNECTION.asString())).thenReturn(Collections.emptyEnumeration());
		servlet.findConnectionHeaders(request);
	}

}
