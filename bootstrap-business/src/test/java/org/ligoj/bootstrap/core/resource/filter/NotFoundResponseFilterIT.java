/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.AbstractRestTest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * ContainerResponseFilter resource test, includes {@link NotFoundResponseFilter}
 */
class NotFoundResponseFilterIT extends AbstractRestTest {

	/**
	 * URI
	 */
	private static final String RESOURCE = "/filter";

	/**
	 * Remote REST server.
	 */
	private static Server server;

	/**
	 * server creation.
	 */
	@BeforeAll
	 static void startServer() {
		server = new NotFoundResponseFilterIT().startRestServer("");
	}

	@Test
	 void testReturnNull() throws IOException {
		final var httpGet = new HttpGet(BASE_URI + RESOURCE + "/null");
		httpGet.addHeader("sm_universalid", DEFAULT_USER);
		httpclient.execute(httpGet, response -> {
			Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, response.getCode());
			Assertions.assertNull(response.getEntity());
			return null;
		});
	}

	@Test
	 void testReturnNull404() throws IOException {
		final var httpGet = new HttpGet(BASE_URI + RESOURCE + "/null404");
		httpGet.addHeader("sm_universalid", DEFAULT_USER);
		httpclient.execute(httpGet, response -> {
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
			Assertions.assertEquals("{\"code\":\"data\"}", IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
			return null;
		});
	}

	@Test
	void testReturnNull404Id() throws IOException {
		final var httpGet = new HttpGet(BASE_URI + RESOURCE + "/null404/789");
		httpGet.addHeader("sm_universalid", DEFAULT_USER);
		httpclient.execute(httpGet, response -> {
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
			Assertions.assertEquals("{\"code\":\"entity\",\"message\":\"789\"}",
					IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
			return null;
		});
	}

	@Test
	void testReturnNotNull() throws IOException {
		final var httpGet = new HttpGet(BASE_URI + RESOURCE + "/not-null");
		httpGet.addHeader("sm_universalid", DEFAULT_USER);
		httpclient.execute(httpGet, response -> {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());
			try (InputStream input = response.getEntity().getContent()) {
				final String content = IOUtils.toString(input, StandardCharsets.UTF_8);
				Assertions.assertEquals("string", content);
			}
			return null;
		});
	}

	/**
	 * shutdown server
	 */
	@AfterAll
	static void tearDown() throws Exception {
		server.stop();
	}
}
