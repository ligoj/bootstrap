/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.HttpMethod;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * CURL processor.
 */
@Slf4j
public class CurlProcessor implements AutoCloseable {

	/**
	 * Dummy SSL manager.
	 */
	public static class TrustedX509TrustManager implements javax.net.ssl.X509TrustManager {
		@Override
		public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
			// Ignore this, it's OK
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
			// Yes we trust
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
	/**
	 * Proxy configuration constants
	 */
	private static final String HTTPS_PROXY_PORT = "https.proxyPort";

	private static final String HTTPS_PROXY_HOST = "https.proxyHost";

	/**
	 * Default callback.
	 */
	private static final DefaultHttpResponseCallback DEFAULT_CALLBACK = new DefaultHttpResponseCallback();

	/**
	 * Support HTTP methods.
	 */
	private static final Map<String, Class<?>> SUPPORTED_METHOD = new HashMap<>();

	static {
		SUPPORTED_METHOD.put(HttpMethod.GET, HttpGet.class);
		SUPPORTED_METHOD.put(HttpMethod.POST, HttpPost.class);
		SUPPORTED_METHOD.put(HttpMethod.PUT, HttpPut.class);
		SUPPORTED_METHOD.put(HttpMethod.DELETE, HttpDelete.class);
	}

	/**
	 * Return a trusted TLS registry.
	 *
	 * @return a trusted TLS registry.
	 */
	public static Registry<ConnectionSocketFactory> newSslContext() {
		return newSslContext("TLS");
	}

	/**
	 * Return a trusted SSL registry using the given protocol.
	 *
	 * @param protocol
	 *            The SSL protocol.
	 * @return A new trusted SSL registry using the given protocol.
	 */
	protected static Registry<ConnectionSocketFactory> newSslContext(final String protocol) {
		// Initialize HTTPS scheme
		final TrustManager[] allCerts = new TrustManager[] { new TrustedX509TrustManager() };
		try {
			final SSLContext sslContext = SSLContext.getInstance(protocol);
			sslContext.init(null, allCerts, new SecureRandom());
			final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
					NoopHostnameVerifier.INSTANCE);
			return RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslSocketFactory)
					.register("http", PlainConnectionSocketFactory.getSocketFactory()).build();
		} catch (final GeneralSecurityException e) {
			// Wrap the exception
			throw new IllegalStateException("Unable to build a secured " + protocol + " registry", e);
		}
	}

	/**
	 * Create a new processor, check the URL, and if failed, throw a {@link ValidationJsonException}
	 *
	 * @param url
	 *            The URL to check.
	 * @param propertyName
	 *            Name of the validation JSon property
	 * @param errorText
	 *            I18N key of the validation message.
	 */
	public static void validateAndClose(final String url, final String propertyName, final String errorText) {
		try (final CurlProcessor curlProcessor = new CurlProcessor()) {
			curlProcessor.validate(url, propertyName, errorText);
		}
	}

	@Getter
	protected final CloseableHttpClient httpClient;

	protected final HttpClientBuilder clientBuilder;

	@Setter
	protected HttpResponseCallback callback;

	/**
	 * Optional replay on rejected response. A request can only be replayed once.
	 */
	@Setter
	protected Function<CurlRequest, Boolean> replay;

	/**
	 * Prepare a processor without callback on response.
	 */
	public CurlProcessor() {
		this(DEFAULT_CALLBACK);
	}

	/**
	 * Prepare a processor with callback.
	 *
	 * @param callback
	 *            Not <code>null</code> {@link HttpResponseCallback} used for each response.
	 */
	public CurlProcessor(final HttpResponseCallback callback) {
		this.callback = callback;
		this.clientBuilder = HttpClientBuilder.create();

		// Initialize proxy
		final String proxyHost = System.getProperty(HTTPS_PROXY_HOST);
		HttpHost proxy = null;
		if (proxyHost != null) {
			proxy = new HttpHost(proxyHost, Integer.parseInt(System.getProperty(HTTPS_PROXY_PORT)));
		}

		// Initialize connection manager
		final HttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(newSslContext());
		clientBuilder.setConnectionManager(connectionManager);
		clientBuilder.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
				.setRedirectsEnabled(false).setSocketTimeout(20000).setProxy(proxy).build());

		// Initialize cookie strategy
		final CookieStore cookieStore = new BasicCookieStore();
		final HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		clientBuilder.setDefaultCookieStore(cookieStore);

		// Initialize the HTTP client without 302
		this.httpClient = clientBuilder.disableRedirectHandling().build();
	}

	/**
	 * Add headers to HTTP request depending on the content-type and content.
	 */
	private void addHeaders(final CurlRequest request, final String content, final HttpRequestBase httpRequest) {
		if (StringUtils.isNotEmpty(content)) {
			// Add the content
			((HttpEntityEnclosingRequest) httpRequest).setEntity(new StringEntity(content, StandardCharsets.UTF_8));

			// Add content-type header if not provided
			addSingleValuedHeader(request, httpRequest, "Content-Type", "application/x-www-form-urlencoded");
		}

		// Add charset if not provided
		addSingleValuedHeader(request, httpRequest, "Accept-Charset", "utf-8");

		// Add headers
		for (final Entry<String, String> header : request.getHeaders().entrySet()) {
			httpRequest.addHeader(header.getKey(), header.getValue());
		}
	}

	/**
	 * Add a header if not defined in <param>request</param>.
	 *
	 * @param request
	 *            The user defined request.
	 * @param httpRequest
	 *            The target HTTP request.
	 * @param header
	 *            The single valued header to add.
	 * @param defaultHeader
	 *            The default value of header to add.
	 */
	private void addSingleValuedHeader(final CurlRequest request, final HttpRequestBase httpRequest,
			final String header, final String defaultHeader) {
		// Look the headers, ignoring case for the header to add
		if (request.getHeaders().keySet().stream().noneMatch(header::equalsIgnoreCase)) {
			// Default header
			httpRequest.addHeader(header, defaultHeader);
		}
	}

	/**
	 * Call the HTTP method.
	 *
	 * @param request
	 *            The request to process.
	 * @param url
	 *            The URL to call.
	 * @return <code>true</code> when the call succeed.
	 * @throws Exception
	 *             When process failed at protocol level or timeout.
	 */
	protected boolean call(final CurlRequest request, final String url) throws Exception { // NOSONAR - Many Exception
		final HttpRequestBase httpRequest = (HttpRequestBase) SUPPORTED_METHOD.get(request.getMethod())
				.getConstructor(String.class).newInstance(url);
		addHeaders(request, request.getContent(), httpRequest);

		// Timeout management
		if (request.getTimeout() != null) {
			// Hard timeout has been set
			final TimerTask task = new TimerTask() {
				@Override
				public void run() {
					// Abort the query if not yet completed...
					httpRequest.abort();
				}
			};
			new Timer(true).schedule(task, request.getTimeout());
		}

		// Execute the request
		try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
			// Save the status
			request.setStatus(response.getStatusLine().getStatusCode());

			// Ask for the callback a flow control
			return ObjectUtils.defaultIfNull(request.getCallback(), callback).onResponse(request, response);
		}
	}

	/**
	 * Close the connection.
	 */
	@Override
	public void close() {
		try {
			getHttpClient().close();
		} catch (IOException e) {
			// Ignore
		}
	}

	/**
	 * Execute a GET and return the content.
	 *
	 * @param url
	 *            The GET URL.
	 * @param headers
	 *            Optional headers <code>name:value</code>.
	 * @return the response if there is no error, or <code>null</code>/
	 */
	public String get(final String url, final String... headers) {
		final CurlRequest curlRequest = new CurlRequest(HttpMethod.GET, url, null, headers);
		curlRequest.setSaveResponse(true);
		process(curlRequest);
		return curlRequest.getResponse();
	}

	/**
	 * Execute the given requests.
	 *
	 * @param requests
	 *            the request to proceed.
	 * @return <code>true</code> if the process succeed.
	 */
	public boolean process(final CurlRequest... requests) {

		// Log file base 0 counter
		int counter = 0;
		for (final CurlRequest request : requests) {
			// Update the counter
			request.counter = counter++;

			// Process the request
			if (!process(request)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Process the given request.
	 *
	 * @param request
	 *            The request to process.
	 * @return <code>true</code> when the call succeed.
	 */
	protected boolean process(final CurlRequest request) {
		final String url = request.getUrl();

		// Expose the current processor to this request
		request.processor = this;
		try {
			boolean result = call(request, url);
			if (!result && replay != null) {
				// Replay as needed this request
				result = replay.apply(request) && call(request, url);
			}
			return result;
		} catch (final Exception e) { // NOSONAR - This exception can be dropped
			log.error("Request execution ' [{}] {} {}' failed : {}", request.getCounter(), request.getMethod(), url,
					e.getMessage());
		}
		return false;
	}

	/**
	 * Execute the given requests. Cookies are kept along this execution and the next ones associated to this processor.
	 *
	 * @param requests
	 *            the request to proceed.
	 * @return <code>true</code> if the process succeed.
	 */
	public boolean process(final List<CurlRequest> requests) {
		return process(requests.toArray(new CurlRequest[0]));
	}

	/**
	 * Check the request, and if failed, throw a {@link ValidationJsonException}
	 *
	 * @param request
	 *            The request to check.
	 * @param propertyName
	 *            Name of the validation JSon property
	 * @param errorText
	 *            I18N key of the validation message.
	 */
	public void validate(final CurlRequest request, final String propertyName, final String errorText) {
		if (!process(request)) {
			throw new ValidationJsonException(propertyName, errorText);
		}
	}

	/**
	 * Check the URL, and if failed, throw a {@link ValidationJsonException}
	 *
	 * @param url
	 *            The URL to check.
	 * @param propertyName
	 *            Name of the validation JSon property name for {@link ValidationJsonException} when the check fails.
	 * @param errorText
	 *            I18N key of the validation message when the check fails.
	 */
	public void validate(final String url, final String propertyName, final String errorText) {
		validate(new CurlRequest(HttpMethod.GET, url, null), propertyName, errorText);
	}
}
