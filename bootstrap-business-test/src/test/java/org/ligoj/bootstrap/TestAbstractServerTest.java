/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Test class of {@link AbstractServerTest}
 */
class TestAbstractServerTest extends AbstractServerTest {

	/**
	 * Only there for coverage, no Spring involved.
	 */
	@Test
	void startAutoStop() throws IOException {
		httpServer.stubFor(
				WireMock.get(WireMock.urlPathEqualTo("/")).willReturn(WireMock.aResponse().withStatus(HttpStatus.SC_OK).withBody("ok")));
		httpServer.start();

		Assertions.assertEquals("ok", IOUtils.toString(URI.create("http://localhost:" + MOCK_PORT + "/"), StandardCharsets.UTF_8));
	}

	/**
	 * Only there for coverage, no Spring involved.
	 */
	@Test
	void startStop() throws IOException {
		httpServer.stubFor(
				WireMock.get(WireMock.urlPathEqualTo("/")).willReturn(WireMock.aResponse().withStatus(HttpStatus.SC_OK).withBody("ok")));
		httpServer.start();
		Assertions.assertEquals("ok", IOUtils.toString(URI.create("http://localhost:" + MOCK_PORT + "/"), StandardCharsets.UTF_8));
		httpServer.stop();
		httpServer = null;
	}

	/**
	 * Only there for coverage, no Spring involved.
	 */
	@Test
	void startStop2() throws IOException {
		httpServer.stubFor(
				WireMock.get(WireMock.urlPathEqualTo("/")).willReturn(WireMock.aResponse().withStatus(HttpStatus.SC_OK).withBody("ok")));
		httpServer.start();
		Assertions.assertEquals("ok", IOUtils.toString(URI.create("http://localhost:" + MOCK_PORT + "/"), StandardCharsets.UTF_8));
		httpServer.stop();
	}

	/**
	 * Stop a stopped server.
	 */
	@Test
	void stopTwice() {
		httpServer = Mockito.mock(WireMockServer.class);
		Mockito.doThrow(new RuntimeException()).when(httpServer).stop();
		shutDownMockServer();
		Mockito.verify(httpServer, Mockito.times(1)).stop();
	}

	/**
	 * Only there for coverage, no Spring involved.
	 */
	@Test
	void prepareMockServerDuplicate() {
		Assertions.assertThrows(IllegalStateException.class, this::prepareMockServer);
	}
}
