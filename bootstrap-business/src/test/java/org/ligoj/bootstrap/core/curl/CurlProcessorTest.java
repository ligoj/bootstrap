/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.curl.CurlProcessor;
import org.ligoj.bootstrap.core.curl.CurlRequest;
import org.ligoj.bootstrap.core.curl.DefaultHttpResponseCallback;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;
import org.mockito.Mockito;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

/**
 * Test class of {@link CurlProcessor}
 */
public class CurlProcessorTest extends org.ligoj.bootstrap.AbstractServerTest {

	/**
	 * port used for proxy
	 */
	private static final int PROXY_PORT = 8122;

	@Test
	public void testX509() {
		final CurlProcessor.TrustedX509TrustManager x509TrustManager = new CurlProcessor.TrustedX509TrustManager();
		x509TrustManager.checkClientTrusted(null, null);
		x509TrustManager.checkServerTrusted(null, null);
		Assertions.assertEquals(0, x509TrustManager.getAcceptedIssuers().length);
		try (final CurlProcessor processor = new CurlProcessor()) {
			Assertions.assertNotNull(processor.getHttpClient());
		}
	}

	@Test
	public void testX509Failed() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			CurlProcessor.newSslContext("none");
		});
	}

	@Test
	public void testGet() {
		httpServer.stubFor(
				get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		try (final CurlProcessor processor = new CurlProcessor()) {
			final String downloadPage = processor.get("http://localhost:" + MOCK_PORT);
			Assertions.assertEquals("CONTENT", downloadPage);
		}
	}

	@Test
	public void validate() {
		httpServer.stubFor(
				get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		CurlProcessor.validateAndClose("http://localhost:" + MOCK_PORT, "any", "any");
	}

	@Test
	public void validateFail() {
		httpServer.stubFor(get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_BAD_REQUEST)));
		httpServer.start();

		final ValidationJsonException vse = Assertions.assertThrows(ValidationJsonException.class,
				() -> CurlProcessor.validateAndClose("http://localhost:" + MOCK_PORT, "parameter", "value"));
		Assertions.assertEquals("value", vse.getErrors().get("parameter").get(0).get("rule"));
	}

	@Test
	public void testGetFailed() {
		final CurlRequest curlRequest = new CurlRequest(null, "http://localhost:" + MOCK_PORT);
		curlRequest.setSaveResponse(true);
		try (final CurlProcessor processor = new CurlProcessor()) {
			processor.process(curlRequest);
		}
		Assertions.assertNull(curlRequest.getResponse());

		// Request has not been sent
		Assertions.assertEquals(0, curlRequest.getStatus());
	}

	@Test
	public void testPost() {
		httpServer.stubFor(
				post(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		final CurlRequest curlRequest = new CurlRequest("POST", "http://localhost:" + MOCK_PORT, "CONTENT");
		curlRequest.setSaveResponse(true);
		try (final CurlProcessor processor = new CurlProcessor()) {
			Assertions.assertTrue(processor.process(curlRequest));
		}
		Assertions.assertEquals("CONTENT", curlRequest.getResponse());
		Assertions.assertEquals(200, curlRequest.getStatus());
	}

	@Test
	public void process() {
		httpServer.stubFor(
				post(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		final CurlProcessor processor = new CurlProcessor();

		// Would succeed
		final CurlRequest curlRequest1 = new CurlRequest("POST", "http://localhost:" + MOCK_PORT, "CONTENT");
		curlRequest1.setSaveResponse(true);

		// Would fail
		final CurlRequest curlRequest2 = new CurlRequest("PUT", "http://localhost:" + MOCK_PORT, "CONTENT");
		curlRequest2.setSaveResponse(true);

		// Never executed
		final CurlRequest curlRequest3 = new CurlRequest("POST", "http://localhost:" + MOCK_PORT, "CONTENT");
		curlRequest3.setSaveResponse(true);

		// Process
		Assertions.assertFalse(processor.process(curlRequest1, curlRequest2, curlRequest3));
		Assertions.assertEquals("CONTENT", curlRequest1.getResponse());
		Assertions.assertEquals(200, curlRequest1.getStatus());
		Assertions.assertNull(curlRequest2.getResponse());
		Assertions.assertEquals(404, curlRequest2.getStatus());
		Assertions.assertNull(curlRequest3.getResponse());
		Assertions.assertEquals(0, curlRequest3.getStatus());
		Assertions.assertSame(processor, curlRequest1.getProcessor());
		Assertions.assertSame(processor, curlRequest2.getProcessor());
		Assertions.assertNull(curlRequest3.getProcessor());
	}

	@Test
	public void processTimeout() {
		httpServer.stubFor(post(urlPathEqualTo("/success"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.stubFor(post(urlPathEqualTo("/timeout"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT").withFixedDelay(4000)));
		httpServer.start();

		long start = System.currentTimeMillis();

		// Would succeed
		final CurlRequest curlRequest1 = new CurlRequest("POST", "http://localhost:" + MOCK_PORT + "/success",
				"CONTENT");
		curlRequest1.setTimeout(500);
		curlRequest1.setSaveResponse(true);

		// Would fail timeout
		final CurlRequest curlRequest2 = new CurlRequest("POST", "http://localhost:" + MOCK_PORT + "/timeout",
				"CONTENT");
		curlRequest2.setTimeout(500);
		curlRequest2.setSaveResponse(true);

		// Process
		try (final CurlProcessor processor = new CurlProcessor()) {
			Assertions.assertFalse(processor.process(curlRequest1, curlRequest2));
			Assertions.assertEquals("CONTENT", curlRequest1.getResponse());
			Assertions.assertNull(curlRequest2.getResponse());
			Assertions.assertTrue(System.currentTimeMillis() - start <= 1000);
		}
	}

	@Test
	public void testHeaders() {
		httpServer.stubFor(
				get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		try (final CurlProcessor processor = new CurlProcessor()) {
			Assertions.assertEquals("CONTENT",
					processor.get("http://localhost:" + MOCK_PORT, "Content-Type:text/html"));
		}
	}

	@Test
	public void testHeadersOverrideDefault() {
		httpServer.stubFor(get(urlPathEqualTo("/")).withHeader("ACCEPT-charset", new EqualToPattern("utf-8"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		try (final CurlProcessor processor = new CurlProcessor()) {
			Assertions.assertEquals("CONTENT", processor.get("http://localhost:" + MOCK_PORT, "ACCEPT-charset:utf-8"));
		}
	}

	@Test
	public void testGetRedirected() {
		httpServer.stubFor(get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_MOVED_TEMPORARILY)
				.withHeader("Location", "http://www.google.fr")));
		httpServer.start();

		try (final CurlProcessor processor = new CurlProcessor()) {
			final String downloadPage = processor.get("http://localhost:" + MOCK_PORT);
			Assertions.assertNull(downloadPage);
		}
	}

	@Test
	public void processNext() {
		httpServer.stubFor(
				get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		final List<CurlRequest> requests = new ArrayList<>();
		final CurlRequest curlRequest = new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null);
		curlRequest.setSaveResponse(true);
		requests.add(curlRequest);
		requests.add(new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null));

		try (final CurlProcessor processor = new CurlProcessor()) {
			Assertions.assertTrue(processor.process(requests));
			Assertions.assertEquals("CONTENT", curlRequest.getResponse());

			// Continue the execution
			processor.setCallback(new DefaultHttpResponseCallback());
			Assertions.assertTrue(processor.process(curlRequest));
			Assertions.assertEquals("CONTENT", curlRequest.getResponse());
		}
	}

	@Test
	public void processReplay() {
		httpServer.stubFor(get(urlPathEqualTo("/")).inScenario("replay").whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.SC_UNAUTHORIZED)).willSetStateTo("second-attempt"));
		httpServer.stubFor(get(urlPathEqualTo("/")).inScenario("replay").whenScenarioStateIs("second-attempt")
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		final List<CurlRequest> requests = new ArrayList<>();

		try (final CurlProcessor processor = new CurlProcessor()) {
			processor.setReplay(r -> r.getStatus() == 401);
			final CurlRequest curlRequest = new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null);
			curlRequest.setSaveResponse(true);
			requests.add(curlRequest);
			requests.add(new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null));
			Assertions.assertTrue(processor.process(requests));
			Assertions.assertEquals("CONTENT", curlRequest.getResponse());

			// Continue the execution
			processor.setCallback(new DefaultHttpResponseCallback());
			Assertions.assertTrue(processor.process(curlRequest));
			Assertions.assertEquals("CONTENT", curlRequest.getResponse());
		}
	}

	/**
	 * Replay conditions are not met.
	 */
	@Test
	public void processReplayRejected() {
		httpServer.stubFor(get(urlPathEqualTo("/")).inScenario("replay").whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.SC_UNAUTHORIZED)).willSetStateTo("second-attempt"));
		httpServer.stubFor(get(urlPathEqualTo("/")).inScenario("replay").whenScenarioStateIs("second-attempt")
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		final List<CurlRequest> requests = new ArrayList<>();

		try (final CurlProcessor processor = new CurlProcessor()) {
			processor.setReplay(r -> false);
			final CurlRequest curlRequest = new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null);
			curlRequest.setSaveResponse(true);
			requests.add(curlRequest);
			requests.add(new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null));
			Assertions.assertFalse(processor.process(requests));
		}
	}

	/**
	 * Replay failed at the second attempt
	 */
	@Test
	public void processReplayFailed() {
		httpServer.stubFor(get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_UNAUTHORIZED)));
		httpServer.start();

		final List<CurlRequest> requests = new ArrayList<>();

		try (final CurlProcessor processor = new CurlProcessor()) {
			processor.setReplay(r -> true);
			final CurlRequest curlRequest = new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null);
			curlRequest.setSaveResponse(true);
			requests.add(curlRequest);
			requests.add(new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null));
			Assertions.assertFalse(processor.process(requests));
		}
	}

	@Test
	public void processCallbackFails() {
		httpServer.stubFor(
				get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		final List<CurlRequest> requests = new ArrayList<>();

		try (final CurlProcessor processor = new CurlProcessor()) {
			final CurlRequest curlRequest = new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null);
			curlRequest.setSaveResponse(true);
			requests.add(curlRequest);
			requests.add(new CurlRequest("GET", "http://localhost:" + MOCK_PORT, null));
			Assertions.assertTrue(processor.process(requests));
			Assertions.assertEquals("CONTENT", curlRequest.getResponse());

			// Continue the execution
			processor.setCallback((req, resp) -> {
				throw new IllegalStateException();
			});
			Assertions.assertFalse(processor.process(curlRequest));
		}
	}

	@Test
	public void testProxy() {
		// set proxy configuration and proxy server
		System.setProperty("https.proxyHost", "localhost");
		System.setProperty("https.proxyPort", String.valueOf(PROXY_PORT));
		final WireMockServer proxyServer = new WireMockServer(PROXY_PORT);
		proxyServer.stubFor(
				get(WireMock.urlMatching(".*")).willReturn(aResponse().proxiedFrom("http://localhost:" + MOCK_PORT)));
		proxyServer.start();

		// set main http server
		httpServer.stubFor(
				get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(HttpStatus.SC_OK).withBody("CONTENT")));
		httpServer.start();

		// launch request
		try (final CurlProcessor processor = new CurlProcessor()) {
			final String downloadPage = processor.get("http://localhost:" + PROXY_PORT);
			Assertions.assertEquals("CONTENT", downloadPage);
			// clean proxy configuration
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
			proxyServer.stop();
		}
	}

	@Test
	public void closeTwice() {
		try (final CurlProcessor processor = new CurlProcessor()) {
			processor.close();
		}
	}

	@Test
	public void closeErrorTwice() throws IOException {
		final CloseableHttpClient mock = Mockito.mock(CloseableHttpClient.class);
		Mockito.doThrow(new IOException()).when(mock).close();
		try (final CurlProcessor processor = new CurlProcessor() {
			@Override
			public CloseableHttpClient getHttpClient() {
				return mock;
			}
		}) {
			processor.close();
		}
	}

}
