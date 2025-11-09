/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import jakarta.ws.rs.HttpMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * CURL processor.
 */
@Slf4j
public class CurlProcessor implements AutoCloseable {

	/**
	 * Default connection timeout (ms).
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
	/**
	 * Default response timeout (ms).
	 */
	public static final int DEFAULT_RESPONSE_TIMEOUT = 20000;

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
	 * SSL verify option.
	 */
	private static final String SSL_VERIFY = "ligoj.sslVerify";

	/**
	 * Default callback.
	 */
	public static final DefaultHttpResponseCallback DEFAULT_CALLBACK = new DefaultHttpResponseCallback();

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
	public static Registry<TlsSocketStrategy> newSslContext() {
		return newSslContext("TLS");
	}

	/**
	 * Return a trusted SSL registry using the given protocol.
	 *
	 * @param protocol The SSL protocol.
	 * @return A new trusted SSL registry using the given protocol.
	 */
	protected static Registry<TlsSocketStrategy> newSslContext(final String protocol) {
		// Initialize HTTPS scheme
		final var allCerts = new TrustManager[]{new TrustedX509TrustManager()};
		try {
			final var sslContext = SSLContext.getInstance(protocol);
			sslContext.init(null, allCerts, new SecureRandom());
			final var sslSocketFactory = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
			return RegistryBuilder.<TlsSocketStrategy>create().register(URIScheme.HTTPS.id, sslSocketFactory).build();
		} catch (final GeneralSecurityException e) {
			// Wrap the exception
			throw new IllegalStateException("Unable to build a secured " + protocol + " registry", e);
		}
	}

	/**
	 * Create a new processor, check the URL, and if failed, throw a {@link ValidationJsonException}
	 *
	 * @param url          The URL to check.
	 * @param propertyName Name of the validation JSon property
	 * @param errorText    I18N key of the validation message.
	 */
	public static void validateAndClose(final String url, final String propertyName, final String errorText) {
		validateAndClose(url, propertyName, errorText, false, null, null);
	}

	/**
	 * Create a new processor, check the URL, and if failed, throw a {@link ValidationJsonException}
	 *
	 * @param url          The URL to check.
	 * @param propertyName Name of the validation JSon property
	 * @param errorText    I18N key of the validation message.
	 * @param noVerifySsl  When <code>true</code> SSL checks are ignored.
	 * @param proxyHost    Custom proxy host for this process.
	 * @param proxyPort    Custom proxy port for this process. Default (when <code>null</code>) is <code>8080</code> when proxy host is defined.
	 */
	public static void validateAndClose(final String url, final String propertyName, final String errorText, final boolean noVerifySsl, final String proxyHost, final Integer proxyPort) {
		try (final var curlProcessor = new CurlProcessor(DEFAULT_CALLBACK, DEFAULT_CONNECTION_TIMEOUT,
				DEFAULT_RESPONSE_TIMEOUT, noVerifySsl, proxyHost, proxyPort)) {
			curlProcessor.validate(url, propertyName, errorText);
		}
	}


	@Getter
	protected final CloseableHttpClient httpClient;

	protected final HttpClientBuilder clientBuilder;

	/**
	 * Optional response callback.
	 */
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
	 * @param callback Not <code>null</code> {@link HttpResponseCallback} used for each response.
	 */
	public CurlProcessor(final HttpResponseCallback callback) {
		this(callback, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_RESPONSE_TIMEOUT);
	}

	/**
	 * Prepare a processor with callback.
	 *
	 * @param callback          Not <code>null</code> {@link HttpResponseCallback} used for each response.
	 * @param connectionTimeout Max connection timeout, milliseconds.
	 * @param responseTimeout   Max response timeout, milliseconds.
	 */
	public CurlProcessor(final HttpResponseCallback callback, long connectionTimeout, long responseTimeout) {
		this(callback, connectionTimeout, responseTimeout, false, null, null);
	}

	/**
	 * Prepare a processor with proxy settings.
	 *
	 * @param proxyHost Custom proxy host for this process.
	 * @param proxyPort Custom proxy port for this process. Default (when <code>null</code>) is <code>8080</code> when proxy host is defined.
	 */
	public CurlProcessor(final String proxyHost, final Integer proxyPort) {
		this(DEFAULT_CALLBACK, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_RESPONSE_TIMEOUT, false, proxyHost, proxyPort);
	}

	/**
	 * Prepare a processor with callback.
	 *
	 * @param callback          Not <code>null</code> {@link HttpResponseCallback} used for each response.
	 * @param connectionTimeout Max connection timeout, milliseconds.
	 * @param responseTimeout   Max response timeout, milliseconds.
	 * @param noVerifySsl       When <code>true</code> SSL checks are ignored.
	 * @param proxyHost         Custom proxy host for this process.
	 * @param proxyPort         Custom proxy port for this process. Default (when <code>null</code>) is <code>8080</code> when proxy host is defined.
	 */
	public CurlProcessor(final HttpResponseCallback callback, long connectionTimeout, long responseTimeout, final boolean noVerifySsl, final String proxyHost, final Integer proxyPort) {
		this.callback = callback;
		this.clientBuilder = HttpClientBuilder.create();

		// Initialize proxy
		final var proxyHostResolved = StringUtils.defaultIfBlank(System.getProperty(HTTPS_PROXY_HOST), proxyHost);
		if (proxyHostResolved != null) {
			final var proxyPortResolved = ObjectUtils.getIfNull(proxyPort, StringUtils.defaultIfBlank(System.getProperty(HTTPS_PROXY_PORT), "8080")).toString();
			final var proxy = new HttpHost(proxyHostResolved, Integer.parseInt(proxyPortResolved));
			final var httpRoutePlanner = new DefaultProxyRoutePlanner(proxy);
			clientBuilder.setRoutePlanner(httpRoutePlanner);
		}

		final var verifySslResolved = Boolean.parseBoolean(StringUtils.defaultIfBlank(System.getProperty(SSL_VERIFY), String.valueOf(!noVerifySsl)));
		if (!verifySslResolved) {
			// Initialize connection manager to bypass some SSL checks
			final var connectionManager = BasicHttpClientConnectionManager.create(newSslContext());
			clientBuilder.setConnectionManager(connectionManager);
			clientBuilder.setDefaultRequestConfig(RequestConfig.custom()
					.setCookieSpec(StandardCookieSpec.RELAXED)
					.setRedirectsEnabled(false)
					.setResponseTimeout(responseTimeout, TimeUnit.MILLISECONDS)
					.setConnectionRequestTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
					.build());
		}


		// Initialize cookie strategy
		final var cookieStore = new BasicCookieStore();
		final var context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		clientBuilder.setDefaultCookieStore(cookieStore);

		// Initialize the HTTP client without 302
		this.httpClient = clientBuilder.disableRedirectHandling().build();
	}

	/**
	 * Add headers to HTTP request depending on the content-type and content.
	 */
	private void addHeaders(final CurlRequest request, final String content, final HttpUriRequestBase httpRequest) {
		if (StringUtils.isNotEmpty(content)) {
			// Add the content
			httpRequest.setEntity(new StringEntity(content, StandardCharsets.UTF_8));

			// Add content-type header if not provided
			addSingleValuedHeader(request, httpRequest, "Content-Type", "application/x-www-form-urlencoded");
		}

		// Add charset if not provided
		addSingleValuedHeader(request, httpRequest, "Accept-Charset", "utf-8");

		// Add headers
		for (final var header : request.getHeaders().entrySet()) {
			httpRequest.addHeader(header.getKey(), header.getValue());
		}
	}

	/**
	 * Add a header if not defined in <param>request</param>.
	 *
	 * @param request       The user defined request.
	 * @param httpRequest   The target HTTP request.
	 * @param header        The single valued header to add.
	 * @param defaultHeader The default value of header to add.
	 */
	private void addSingleValuedHeader(final CurlRequest request, final HttpUriRequestBase httpRequest,
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
	 * @param request The request to process.
	 * @param url     The URL to call.
	 * @return <code>true</code> when the call succeed.
	 * @throws Exception When process failed at protocol level or timeout.
	 */
	protected boolean call(final CurlRequest request, final String url) throws Exception { // NOSONAR - Many Exception
		final var httpRequest = (HttpUriRequestBase) SUPPORTED_METHOD.get(request.getMethod()).getConstructor(String.class)
				.newInstance(url);
		addHeaders(request, request.getContent(), httpRequest);

		// Timeout management
		if (request.getTimeout() != null) {
			// Hard timeout has been set
			final var task = new TimerTask() {
				@Override
				public void run() {
					// Abort the query if not yet completed...
					httpRequest.abort();
				}
			};
			new Timer(true).schedule(task, request.getTimeout());
		}

		// Execute the request
		//noinspection
		return httpClient.execute(httpRequest, response -> {
			// Save the status
			request.setStatus(response.getCode());

			// Ask for the callback a flow control
			return ObjectUtils.getIfNull(request.getCallback(), callback).onResponse(request, response);
		});
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
	 * @param url     The GET URL.
	 * @param headers Optional headers <code>name:value</code>.
	 * @return the response if there is no error, or <code>null</code>/
	 */
	public String get(final String url, final String... headers) {
		final var curlRequest = new CurlRequest(HttpMethod.GET, url, null, headers);
		curlRequest.setSaveResponse(true);
		process(curlRequest);
		return curlRequest.getResponse();
	}

	/**
	 * Execute the given requests.
	 *
	 * @param requests the request to proceed.
	 * @return <code>true</code> if the process succeed.
	 */
	public boolean process(final CurlRequest... requests) {

		// Log file base 0 counter
		var counter = 0;
		for (final var request : requests) {
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
	 * @param request The request to process.
	 * @return <code>true</code> when the call succeed.
	 */
	protected boolean process(final CurlRequest request) {
		final var url = request.getUrl();

		// Expose the current processor to this request
		request.processor = this;
		try {
			var result = call(request, url);
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
	 * @param requests the request to proceed.
	 * @return <code>true</code> if the process succeed.
	 */
	public boolean process(final List<CurlRequest> requests) {
		return process(requests.toArray(new CurlRequest[0]));
	}

	/**
	 * Check the request, and if failed, throw a {@link ValidationJsonException}
	 *
	 * @param request      The request to check.
	 * @param propertyName Name of the validation JSon property
	 * @param errorText    I18N key of the validation message.
	 */
	public void validate(final CurlRequest request, final String propertyName, final String errorText) {
		if (!process(request)) {
			throw new ValidationJsonException(propertyName, errorText);
		}
	}

	/**
	 * Check the URL, and if failed, throw a {@link ValidationJsonException}
	 *
	 * @param url          The URL to check.
	 * @param propertyName Name of the validation JSon property name for {@link ValidationJsonException} when the check
	 *                     fails.
	 * @param errorText    I18N key of the validation message when the check fails.
	 */
	public void validate(final String url, final String propertyName, final String errorText) {
		validate(new CurlRequest(HttpMethod.GET, url, null), propertyName, errorText);
	}
}
