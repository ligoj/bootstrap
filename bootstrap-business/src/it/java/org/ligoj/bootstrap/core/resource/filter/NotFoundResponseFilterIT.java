package org.ligoj.bootstrap.core.resource.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.ligoj.bootstrap.AbstractRestTest;

/**
 * ContainerResponseFilter resource test, includes {@link NotFoundResponseFilter}
 */
public class NotFoundResponseFilterIT extends AbstractRestTest {

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
	public static void startServer() {
		server = new NotFoundResponseFilterIT().startRestServer("./src/test/resources/WEB-INF/web-test-nodb.xml");
	}

	@Test
	public void testReturnNull() throws IOException {
		final HttpGet httpGet = new HttpGet(BASE_URI + RESOURCE + "/null");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());
			Assertions.assertNull(response.getEntity());
		} finally {
			if (response != null && response.getEntity() != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	@Test
	public void testReturnNull404() throws IOException {
		final HttpGet httpGet = new HttpGet(BASE_URI + RESOURCE + "/null404");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			Assertions.assertEquals("{\"code\":\"data\"}", IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
		} finally {
			if (response != null && response.getEntity() != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	@Test
	public void testReturnNull404Id() throws IOException {
		final HttpGet httpGet = new HttpGet(BASE_URI + RESOURCE + "/null404/789");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			Assertions.assertEquals("{\"code\":\"entity\",\"message\":\"789\"}",
					IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
		} finally {
			if (response != null && response.getEntity() != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	@Test
	public void testReturnNotNull() throws IOException {
		final HttpGet httpGet = new HttpGet(BASE_URI + RESOURCE + "/not-null");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertEquals("string", content);
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * shutdown server
	 */
	@AfterAll
	public static void tearDown() throws Exception {
		server.stop();
	}
}
