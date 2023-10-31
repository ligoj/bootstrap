/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.proxy;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.ee10.proxy.ProxyServlet;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reverse proxy for business server.
 */
@Slf4j
public class BackendProxyServlet extends ProxyServlet {

	/**
	 * Header forwarded to back-end and containing the principal username or declared API user that will be checked on
	 * the other side.
	 */
	private static final String HEADER_USER = "SM_UniversalID".toUpperCase();

	private static final String COOKIE_JEE = "JSessionID".toUpperCase();

	private static final String HEADER_COOKIE = "cookie";

	/**
	 * Headers will not be forwarded from the back-end.
	 */
	private static final String[] IGNORE_HEADERS = {"expires", "x-content-type-options", "server",
			"visited", "date", "x-frame-options", "x-xss-protection", "pragma", "cache-control"};

	/**
	 * Header will be ignored when the value starts with the
	 */
	private static final Map<String, String> IGNORE_HEADER_VALUE = new HashMap<>();

	static {
		IGNORE_HEADER_VALUE.put("set-cookie", COOKIE_JEE);
	}

	/**
	 * Managed plain page error.
	 */
	private static final Map<Integer, Integer> MANAGED_PLAIN_ERROR = new HashMap<>();

	// Initialize the mappings
	static {
		MANAGED_PLAIN_ERROR.put(HttpServletResponse.SC_NOT_FOUND, HttpServletResponse.SC_NOT_FOUND);
		MANAGED_PLAIN_ERROR.put(HttpServletResponse.SC_METHOD_NOT_ALLOWED, HttpServletResponse.SC_NOT_FOUND);
		MANAGED_PLAIN_ERROR.put(HttpServletResponse.SC_FORBIDDEN, HttpServletResponse.SC_FORBIDDEN);
		MANAGED_PLAIN_ERROR.put(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		MANAGED_PLAIN_ERROR.put(HttpServletResponse.SC_BAD_REQUEST, HttpServletResponse.SC_BAD_REQUEST);
		MANAGED_PLAIN_ERROR.put(HttpServletResponse.SC_SERVICE_UNAVAILABLE, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
	}

	/**
	 * SID
	 */
	private static final long serialVersionUID = -3387356144222298075L;

	/**
	 * Target backend's endpoint.
	 */
	private String proxyTo; // NOSONAR - Initialized once, from #init()

	/**
	 * Response buffer size override due to Spring-Security buffer issue.
	 */
	private int responseBufferSize; // NOSONAR - Initialized once, from #init()

	/**
	 * Prefix activating this backend
	 */
	private String prefix; // NOSONAR - Initialized once, from #init()

	/**
	 * Name of query parameter containing the API key
	 */
	private String apiKeyParameter; // NOSONAR - Initialized once, from #init()

	/**
	 * Name of query parameter containing the API username.
	 */
	private String apiUserParameter; // NOSONAR - Initialized once, from #init()

	/**
	 * Name of header containing the API username.
	 */
	private String apiUserHeader; // NOSONAR - Initialized once, from #init()

	/**
	 * Name of header containing the API key.
	 */
	private String apiKeyHeader; // NOSONAR - Initialized once, from #init()

	/**
	 * Pattern capturing the API key to filter.
	 */
	private Pattern apiKeyCleanPattern; // NOSONAR - Initialized once, from #init()

	/**
	 * Pattern capturing the API user to filter.
	 */
	private Pattern apiUserCleanPattern; // NOSONAR - Initialized once, from #init()
	/**
	 * `Access-Control-Allow-Origin` value of response from this route.
	 */
	private String corsOrigin; // NOSONAR - Initialized once, from #init()

	/**
	 * `Vary` value of response from this route.
	 */
	private String corsVary; // NOSONAR - Initialized once, from #init()

	private void addHeader(final Request proxyRequest, final String name, final String value) {
		proxyRequest.headers(headers -> headers.add(name, value));
	}

	@Override
	protected void addProxyHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
		super.addProxyHeaders(clientRequest, proxyRequest);

		if (clientRequest.getUserPrincipal() == null) {
			// Forward API user, if defined.
			final var apiUser = getIdData(clientRequest, apiUserParameter, apiUserHeader);
			final var apiKey = getIdData(clientRequest, apiKeyParameter, apiKeyHeader);

			if (StringUtils.isNotBlank(apiUser) && StringUtils.isNotBlank(apiKey)) {
				// When there is an API user,
				addHeader(proxyRequest, HEADER_USER, apiUser);
				addHeader(proxyRequest, apiKeyHeader, apiKey);
			}
		} else {
			// Stateful authenticated user
			addHeader(proxyRequest, HEADER_USER, clientRequest.getUserPrincipal().getName());
			addHeader(proxyRequest, "SM_SESSIONID", clientRequest.getSession(false).getId());
		}

		// Forward all cookies but JSESSIONID.
		final var cookies = StringUtils.trimToNull(Arrays.stream(
						Objects.requireNonNullElse(clientRequest.getHeader(HEADER_COOKIE), "").split(";"))
				.map(String::trim).filter(cookie -> !cookie.split("=")[0].trim().equals(COOKIE_JEE))
				.collect(Collectors.joining("; ")));
		if (cookies != null) {
			addHeader(proxyRequest, HEADER_COOKIE, cookies);
		}
	}

	private String getIdData(final HttpServletRequest req, final String parameter, final String header) {
		return ObjectUtils.defaultIfNull(StringUtils.trimToNull(req.getParameter(parameter)),
				StringUtils.trimToNull(req.getHeader(header)));
	}

	@Override
	protected void onProxyResponseFailure(final HttpServletRequest clientRequest,
			final HttpServletResponse proxyResponse, final Response serverResponse, final Throwable failure) {
		_log.warn("Proxy error", failure);

		if (proxyResponse.isCommitted()) {
			// Parent behavior
			super.onProxyResponseFailure(clientRequest, proxyResponse, serverResponse, failure);
		} else {
			proxyResponse.resetBuffer();
			if (failure instanceof TimeoutException) {
				proxyResponse.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
			} else {

				// Unavailable business server as JSON response
				proxyResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				proxyResponse.setContentType("application/json");
				try {
					proxyResponse.getOutputStream()
							.write("{\"code\":\"business-down\"}".getBytes(StandardCharsets.UTF_8));
				} catch (final IOException ioe) {
					_log.warn("Broken proxy stream", ioe);
				}
			}
			proxyResponse.setHeader(HttpHeader.CONNECTION.asString(), HttpHeaderValue.CLOSE.asString());
			final var asyncContext = clientRequest.getAsyncContext();
			asyncContext.complete();
		}
	}

	/**
	 * Check, and return the lower value of given parameter.
	 *
	 * @param parameter the expected "init" parameter.
	 * @return the lower value of given parameter.
	 * @throws UnavailableException when required parameter is not defined.
	 */
	protected String getRequiredInitParameter(final String parameter)
			throws UnavailableException {
		final var value = StringUtils.trimToNull(getServletConfig().getInitParameter(parameter));
		if (value == null) {
			throw new UnavailableException("Init parameter '" + parameter + "' is required.");
		}
		return value;
	}


	@Override
	public void init() throws ServletException {
		super.init();
		final var config = getServletConfig();

		// Read "proxy to" end point URL from "Servlet" configuration and system
		this.prefix = getServletContext().getContextPath() + StringUtils.trimToEmpty(config.getInitParameter("prefix"));

		// Read API configuration
		this.proxyTo = getRequiredInitParameter("proxyTo");
		this.apiUserParameter = getRequiredInitParameter("apiUserParameter");
		this.apiUserHeader = getRequiredInitParameter("apiUserHeader");
		this.apiKeyParameter = getRequiredInitParameter("apiKeyParameter");
		this.apiKeyHeader = getRequiredInitParameter("apiKeyHeader");
		this.apiKeyCleanPattern = newCleanParameter(apiKeyParameter);
		this.apiUserCleanPattern = newCleanParameter(apiUserParameter);
		this.corsOrigin = getRequiredInitParameter("cors-origin");
		this.corsVary = getRequiredInitParameter("cors-vary");
		this.responseBufferSize = Integer.parseInt(getRequiredInitParameter("responseBufferSize"), 10);

		_log.info("Proxying {} --> {}", this.prefix, this.proxyTo);
	}

	/**
	 * New pattern detecting a parameter value inside a query.
	 */
	private Pattern newCleanParameter(final String parameter) {
		return Pattern.compile("^((.*&))?" + parameter + "=[a-zA-Z0-9\\-]+(&(.*))?$");
	}

	@Override
	protected String rewriteTarget(final HttpServletRequest request) {
		var path = request.getRequestURI();
		if (!path.startsWith(this.prefix)) {
			// No match
			return null;
		}

		// Append the query string
		path = newPathWithQueryString(request);

		final var proxyUrl = this.proxyTo + path.substring(this.prefix.length());
		try {
			final var rewrittenURI = new URI(proxyUrl).normalize();
			if (validateDestination(rewrittenURI.getHost(), rewrittenURI.getPort())) {
				// It's a valid and up target
				return rewrittenURI.toString();
			}
		} catch (final URISyntaxException x) {
			// Invalid query
			log.info("Invalid URI {} built from path {}", proxyUrl, request.getRequestURI(), x);
		}
		return null;
	}

	/**
	 * Build a complete URI with original query string, but without API key.
	 */
	private String newPathWithQueryString(final HttpServletRequest request) {
		final var path = request.getRequestURI();
		var query = request.getQueryString();

		if (query == null) {
			// No query, return only the path
			return path;
		}
		if (request.getParameter(apiKeyParameter) == null) {
			// No API Key, return an untouched query string
			return path + "?" + query;
		}

		// Don't forward API key as parameter, only as a header
		query = StringUtils
				.trimToNull(removeApiParameter(removeApiParameter(query, apiKeyCleanPattern), apiUserCleanPattern));
		if (query == null) {
			// The new query, without API key is empty
			return path;
		}

		// Return a query without API key
		return path + "?" + query;
	}

	/**
	 * Remove API key parameter from the given query.
	 */
	private String removeApiParameter(final String query, final Pattern pattern) {
		final var apiMatcher = pattern.matcher(query);
		if (apiMatcher.find()) {
			// API Token is defined as a query parameter, we can remove it
			return ObjectUtils.defaultIfNull(apiMatcher.group(2), "")
					+ ObjectUtils.defaultIfNull(apiMatcher.group(4), "");
		}
		return query;
	}

	@Override
	protected String filterServerResponseHeader(final HttpServletRequest clientRequest, final Response serverResponse,
			final String headerName, final String headerValue) {
		// Filter some headers
		final var lowerCase = StringUtils.lowerCase(headerName);
		return ArrayUtils.contains(IGNORE_HEADERS, lowerCase) || IGNORE_HEADER_VALUE.containsKey(lowerCase)
				&& headerValue.startsWith(IGNORE_HEADER_VALUE.get(lowerCase)) ? null : headerValue;
	}

	/**
	 * Indicates the request was in API or not.
	 *
	 * @param request The original request.
	 * @return <code>true</code> for API request.
	 */
	public static boolean isApiRequest(final HttpServletRequest request) {
		return "XMLHttpRequest".equalsIgnoreCase(StringUtils.trimToEmpty(request.getHeader("X-Requested-With")))
				|| !StringUtils.trimToEmpty(request.getHeader("User-Agent")).contains("Mozilla");
	}

	@Override
	protected void onResponseContent(final HttpServletRequest request, final HttpServletResponse response,
			final Response proxyResponse, final byte[] buffer, final int offset, final int length,
			final Callback callback) {
		final var plainStatus = needPlainPageErrorStatus(request, proxyResponse.getStatus());
		if (plainStatus == 0) {
			super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
		} else {
			try {
				// Standard 404/... page, abort the original response
				final var dispatcher = getServletContext().getRequestDispatcher("/" + plainStatus + ".html");
				dispatcher.forward(getRoot(request), response);
				callback.succeeded();
			} catch (final Exception e) {
				callback.failed(e);
			}
		}
	}

	@Override
	protected void sendProxyRequest(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Request proxyRequest) {
		proxyResponse.setBufferSize(responseBufferSize);
		super.sendProxyRequest(clientRequest, proxyResponse, proxyRequest);
	}

	/**
	 * Is it a 404 like error?
	 *
	 * @param status the current status.
	 * @return the nearest managed status.
	 */
	protected int getManagedPlainPageError(final int status) {
		return ObjectUtils.defaultIfNull(MANAGED_PLAIN_ERROR.get(status), 0);
	}

	/**
	 * Is it a non AJAX 404 like error? .
	 *
	 * @param request the current request.
	 * @param status  the current status.
	 * @return 0 or the status to display.
	 */
	protected int needPlainPageErrorStatus(final HttpServletRequest request, final int status) {
		final var plainStatus = getManagedPlainPageError(status);
		return plainStatus == 0 || isApiRequest(request) ? 0 : plainStatus;
	}

	@Override
	protected void onServerResponseHeaders(final HttpServletRequest request, final HttpServletResponse response,
			final Response proxyResponse) {
		if (needPlainPageErrorStatus(request, proxyResponse.getStatus()) == 0) {
			super.onServerResponseHeaders(request, response, proxyResponse);
		} else {
			// Standard 404 page
			response.addHeader("Content-Type", "text/html");
		}
		response.addHeader("Access-Control-Allow-Origin", corsOrigin);
		response.addHeader("Vary", corsVary);
	}

	/**
	 * Return root request.
	 *
	 * @param request the current request.
	 * @return the root (container) {@link ServletRequest}.
	 */
	protected ServletRequest getRoot(final ServletRequest request) {
		return request instanceof HttpServletRequestWrapper hReq ? getRoot(hReq.getRequest()) : request;
	}

	@Override
	protected Set<String> findConnectionHeaders(final HttpServletRequest clientRequest) {
		final var ignoreRequestHeader = new HashSet<>(
				CollectionUtils.emptyIfNull(super.findConnectionHeaders(clientRequest)));

		// Drop cookie headers forward from FRONT to BACK by default, only filtered ones will be added
		ignoreRequestHeader.add(HEADER_COOKIE);
		return ignoreRequestHeader;
	}
}
