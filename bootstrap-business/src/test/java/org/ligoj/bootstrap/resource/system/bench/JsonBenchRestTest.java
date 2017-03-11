package org.ligoj.bootstrap.resource.system.bench;

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
 * JSON bench REST test.
 */
public class JsonBenchRestTest extends AbstractRestTest {

	/**
	 * URI
	 */
	private static final String RESOURCE = "/system/json-test";

	/**
	 * Remote REST server.
	 */
	private static Server server;

	/**
	 * String array used for bench.
	 */
	private static final String DATA_ARRAY = "[\"value1\",\"value2\"]";

	/**
	 * server creation.
	 */
	@BeforeClass
	public static void startServer() {
		server = new JsonBenchRestTest().startRestServer("./src/test/resources/WEB-INF/web-test-nodb.xml");
	}

	/**
	 * Generic bean with XML annotation.
	 */
	@Test
	public void testPagine() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/pagine");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("{\"recordsTotal\":6,\"recordsFiltered\":5,\"draw\":\"69T\",\"data\":" + DATA_ARRAY + "}", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Generic list of generic beans without XML annotation.
	 */
	@Test
	public void testListGenenric() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/list-generic");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("[{\"key\":\"key1\",\"value\":1},{\"key\":\"key2\",\"value\":2}]", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Generic list of string.
	 */
	@Test
	public void testListString() throws IOException {
		testCollectionString("/list-string");
	}

	/**
	 * Generic array of string.
	 */
	@Test
	public void testArrayString() throws IOException {
		testCollectionString("/array-string");
	}

	/**
	 * Generic element.
	 */
	@Test
	public void testGeneric() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/generic");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("{\"key\":\"key3\",\"value\":3}", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Simple bean.
	 */
	@Test
	public void testBean() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/bean");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("{\"key\":\"keyb\",\"value\":-1}", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Simple map.
	 */
	@Test
	public void testMap() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/map");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("{\"5\":\"value5\",\"6\":\"value6\"}", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Simple GenericEntity of Generic content.
	 */
	@Test
	public void testGenericEntityOfGeneric() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/generic-entity-generic");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("[{\"key\":\"key12\",\"value\":12},{\"key\":\"key13\",\"value\":13}]", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Check simple date handling.
	 */
	@Test
	public void testDate() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/date");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("50", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Check Joda time handling.
	 */
	@Test
	public void testJodaTime() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/datetime");
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("60", content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Simple GenericEntity of {@link String} content.
	 */
	@Test
	public void testGenericEntityOfString() throws IOException {
		testCollectionString("/generic-entity-string");
	}

	/**
	 * Simple bench for 10 items.
	 */
	@Test
	public void testBenchSimpleBean() throws IOException {
		for (int i = 0; i < 7; i++) {
			testBench("/bean-benchmark", (int) Math.pow(10, i));
		}
	}

	/**
	 * Simple bench for 100000 items.
	 */
	@Test
	public void testBenchFinishDummy() {
		// Nothing to do, just there to ensure stop process does not interfere;
	}

	private void testBench(final String resource, final int nb) throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + resource + "/" + nb);
		final long start = System.currentTimeMillis();
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		} finally {
			response.getEntity().getContent().close();
			System.out.println(System.currentTimeMillis() - start);
		}
	}

	private void testCollectionString(final String uri) throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + uri);
		final HttpResponse response = httpclient.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals(DATA_ARRAY, content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * shutdown server
	 */
	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
	}
}
