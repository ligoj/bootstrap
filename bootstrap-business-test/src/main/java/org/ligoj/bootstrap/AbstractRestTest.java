/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

/**
 * An integration test.
 */
@Slf4j
abstract class AbstractRestTest extends AbstractTest {
	/**
	 * URI
	 */
	private static final String PORT = "6380";
	private static final String BASE_URI = "http://localhost:" + PORT + "/bootstrap-business/rest";

	protected static final String DEFAULT_USER = "junit";

	private static final int DEFAULT_RETRIES = 20;

	int retries = DEFAULT_RETRIES;
	CloseableHttpClient httpclient = HttpClientBuilder.create().build();

	/**
	 * Initialize overridden values.
	 */
	@BeforeEach
	public void configureClient() {
		retries = DEFAULT_RETRIES;
		httpclient = HttpClientBuilder.create().build();
	}

	/**
	 * Close client.
	 */
	@AfterEach
	public void closeClient() {
		closeQuietly(httpclient);
	}

	private void initProperties(final String webDescriptor) {
		System.setProperty("jetty.descriptor", webDescriptor);
		System.setProperty("jetty.port", PORT);
		System.setProperty("user.language", "en_en");
		System.setProperty("org.eclipse.jetty.server.webapp.parentLoaderPriority", "true");
	}

	/**
	 * URI used to check the server is UP.
	 *
	 * @return URI used to check the server is UP.
	 */
	private String getPingUri() {
		return BASE_URI + "?_wadl";
	}

	/**
	 * Create a new server and return it. An HTTP access is done to WADL to check the server is started.
	 *
	 * @param webDescriptor location of Jetty Web descriptor file
	 * @return Jetty server object built from the web descriptor.
	 */
	Server startRestServer(final String webDescriptor) {
		initProperties(webDescriptor);
		try {
			final var server = new org.ligoj.bootstrap.http.server.Main().getServer();
			server.start();
			waitForServerReady();

			// Server is started, return the instance
			return server;
		} catch (final Exception e) {
			throw new IllegalStateException("Unable to start correctly the server", e);
		}
	}

	/**
	 * Wait for the server is ready.
	 */
	private void waitForServerReady() throws IOException, InterruptedException {
		final var httpget = new HttpGet(getPingUri());
		CloseableHttpResponse response = null;
		var counter = 0;
		while (true) {
			try {
				if (HttpStatus.SC_OK == httpclient.execute(httpget, HttpResponse::getCode)) {
					break;
				}
				checkRetries(counter);
			} catch (final HttpHostConnectException ex) { // NOSONAR - Wait, and check later
				log.info("Check failed, retrying...");
				checkRetries(counter);
			} finally {
				if (response != null) {
					EntityUtils.consume(response.getEntity());
				}
			}
			counter++;
		}
	}


	/**
	 * Check the maximum reach of tries.
	 */
	private void checkRetries(final int counter) throws InterruptedException {
		if (counter > retries) {
			throw new IllegalStateException("Remote server did not started correctly");
		}
		Thread.sleep(2000);
	}

}
