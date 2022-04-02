/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.proxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.Callback;

import lombok.extern.slf4j.Slf4j;

/**
 * Reverse proxy for business server.
 */
@Slf4j
public class BackendProxyServlet extends ProxyServlet {

	/**
	 * Header forwarded to back-end and containing the principal user name or declared API user that will be checked on
	 * the other side.
	 */
	private static final String HEADER_USER = "SM_UNIVERSALID";

	private static final String COOKIE_JEE = "JSESSIONID";

	private static final String HEADER_COOKIE = "cookie";

	/**
	 * Headers will not be forwarded from the back-end.
	 */
	private static final String[] IGNORE_HEADERS = new String[] { "expires", "x-content-type-options", "server",
			"visited", "date", "x-frame-options", "x-xss-protection", "pragma", "cache-control" };

	/**
	 * Header will be ignore when the value starts with the
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
	 * Prefix activating this backend
	 */
	private String prefix; // NOSONAR - Initialized once, from #init()

	/**
	 * Name of query parameter containing the API key
	 */
	private String apiKeyParameter; // NOSONAR - Initialized once, from #init()

	/**
	 * Name of query parameter containing the API user name.
	 */
	private String apiUserParameter; // NOSONAR - Initialized once, from #init()

	/**
	 * Name of header containing the API user name.
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

	@Override
	protected void addProxyHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
		super.addProxyHeaders(clientRequest, proxyRequest);

		if (clientRequest.getUserPrincipal() != null) {
			// Stateful authenticated user
			proxyRequest.header(HEADER_USER, clientRequest.getUserPrincipal().getName());
			proxyRequest.header("SM_SESSIONID", clientRequest.getSession(false).getId());
		} else {
			// Forward API user, if defined.
			final var apiUser = getIdData(clientRequest, apiUserParameter, apiUserHeader);
			final var apiKey = getIdData(clientRequest, apiKeyParameter, apiKeyHeader);

			if (apiUser != null && apiKey != null) {
				// When there is an API user,
				proxyRequest.header(HEADER_USER, apiUser);
				proxyRequest.header(apiKeyHeader, apiKey);
			}
		}

		// Forward all cookies but JSESSIONID.
		final var cookies = clientRequest.getHeader(HEADER_COOKIE);
		if (cookies != null) {
			proxyRequest.header(HEADER_COOKIE,
					StringUtils.trimToNull(Arrays.stream(cookies.split(";"))
							.filter(cookie -> !cookie.split("=")[0].trim().equals(COOKIE_JEE)).map(String::trim)
							.collect(Collectors.joining("; "))));
		}
	}

	private String getIdData(final HttpServletRequest req, final String parameter, final String header) {
		return ObjectUtils.defaultIfNull(StringUtils.trimToNull(req.getParameter(parameter)),
				StringUtils.trimToNull(req.getHeader(header)));
	}

	@Override
	protected void onProxyResponseFailure(final HttpServletRequest clientRequest,
			final HttpServletResponse proxyResponse, final Response serverResponse, final Throwable failure) {
		_log.warn(failure.toString());

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
	 * @param parameter    the expected "init" parameter.
	 * @param defaultValue the optional default value. May be <code>null</code>.
	 * @return the lower value of given parameter.
	 * @throws UnavailableException when required parameter is not defined.
	 */
	protected String getRequiredInitParameter(final String parameter, final String defaultValue)
			throws UnavailableException {
		final var value = ObjectUtils
				.defaultIfNull(StringUtils.trimToNull(getServletConfig().getInitParameter(parameter)), defaultValue);
		if (value == null) {
			throw new UnavailableException("Init parameter '" + parameter + "' is required.");
		}
		return value;
	}

	/**
	 * Check, and return the lower value of given parameter.
	 * 
	 * @param parameter the expected "init" parameter.
	 * @return the lower value of given parameter.
	 */
	private String getRequiredSystemInitParameter(final String parameter) throws UnavailableException {
		final var parameterValue = StringUtils.trimToNull(getRequiredInitParameter(parameter, null));
		final var value = StringUtils.trimToNull(System.getProperty(parameterValue));
		if (value == null) {
			throw new UnavailableException("Init parameter '" + parameter
					+ "' is defined, but points to a non defined system property '" + parameterValue + "'");
		}
		return value;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		final var config = getServletConfig();

		// Read "proxy to" end point URL from "Servlet" configuration and system
		this.prefix = getServletContext().getContextPath() + StringUtils.trimToEmpty(config.getInitParameter("prefix"));
		this.proxyTo = getRequiredSystemInitParameter("proxyToKey");

		// Read API configuration
		this.apiUserParameter = getRequiredInitParameter("apiUserParameter", "api-user");
		this.apiUserHeader = getRequiredInitParameter("apiUserHeader", "x-api-user");
		this.apiKeyParameter = getRequiredInitParameter("apiKeyParameter", "api-key");
		this.apiKeyHeader = getRequiredInitParameter("apiKeyHeader", "x-api-key");
		this.apiKeyCleanPattern = newCleanParameter(apiKeyParameter);
		this.apiUserCleanPattern = newCleanParameter(apiUserParameter);

		_log.info("Proxying " + this.prefix + " --> " + this.proxyTo);
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

		// Don't forward API key as parameter, only as an header
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
	 * Indicates the request was in AJAX or not.
	 * 
	 * @param request The original request.
	 * @return <code>true</code> for Ajax request.
	 */
	public static boolean isAjaxRequest(final HttpServletRequest request) {
		return "XMLHttpRequest".equalsIgnoreCase(StringUtils.trimToEmpty(request.getHeader("X-Requested-With")));
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
		return plainStatus == 0 || isAjaxRequest(request) ? 0 : plainStatus;
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
		response.addHeader("Access-Control-Allow-Origin", "*");
	}

	/**
	 * Return root request.
	 * 
	 * @param request the current request.
	 * @return the root (container) {@link ServletRequest}.
	 */
	protected ServletRequest getRoot(final ServletRequest request) {
		return request instanceof HttpServletRequestWrapper hreq ? getRoot(hreq.getRequest()) : request;
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
