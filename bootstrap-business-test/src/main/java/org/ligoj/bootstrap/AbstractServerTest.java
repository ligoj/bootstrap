/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Test using mock http server.
 */
public abstract class AbstractServerTest extends AbstractAppTest {
	protected static final int MOCK_PORT = 8120;

	protected WireMockServer httpServer;

	/**
	 * Prepare a Wiremock server.
	 */
	@BeforeEach
	public void prepareMockServer() {
		if (httpServer != null) {
			throw new IllegalStateException("A previous HTTP server was already created");
		}
		httpServer = new WireMockServer(MOCK_PORT);
		System.setProperty("http.keepAlive", "false");
	}

	/**
	 * Ensure the Wiremock server is stopped.
	 */
	@AfterEach
	public void shutDownMockServer() {
		System.clearProperty("http.keepAlive");
		if (httpServer != null) {
			try {
				httpServer.stop();
			} catch (Exception e) {
				// Ignore this error
				log.info("Jetty server did not stopped properly {}", e.getClass().getName());
			}
		}
	}

}
