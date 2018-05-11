/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import lombok.extern.slf4j.Slf4j;

/**
 * An integration test.
 */
@Slf4j
public abstract class AbstractRestTest extends AbstractTest {
	/**
	 * URI
	 */
	protected static final String PORT = "6380";
	protected static final String BASE_URI = "http://localhost:" + PORT + "/bootstrap-business/rest";

	protected static final String DEFAULT_USER = "junit";

	protected static final int DEFAULT_RETRIES = 20;

	protected int retries = DEFAULT_RETRIES;
	protected CloseableHttpClient httpclient = HttpClientBuilder.create().build();

	/**
	 * Initialize overridden values.
	 */
	@BeforeEach
	public void configureCient() {
		retries = DEFAULT_RETRIES;
		httpclient = HttpClientBuilder.create().build();
	}

	/**
	 * Close client.
	 */
	@AfterEach
	public void closeCient() {
		closeQuietly(httpclient);
	}

	protected void initProperties(final String webDescriptor) {
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
	protected String getPingUri() {
		return BASE_URI + "?_wadl";
	}

	/**
	 * Create a new server and return it. A HTTP access is done to WADL to check the server is started.
	 *
	 * @param webDescriptor
	 *            location of Jetty Web descriptor file
	 * @return Jetty server object built from the web descriptor.
	 */
	protected Server startRestServer(final String webDescriptor) {
		initProperties(webDescriptor);
		try {
			final Server server = new org.ligoj.bootstrap.http.server.Main().getServer();
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
		final HttpGet httpget = new HttpGet(getPingUri());
		HttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, ""));
		int counter = 0;
		while (true) {
			try {
				response = httpclient.execute(httpget);
				final int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					break;
				}
				checkRetries(counter);
			} catch (final HttpHostConnectException ex) { // NOSONAR - Wait, and check later
				log.info("Check failed, retrying...");
				checkRetries(counter);
			} finally {
				EntityUtils.consume(response.getEntity());
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
