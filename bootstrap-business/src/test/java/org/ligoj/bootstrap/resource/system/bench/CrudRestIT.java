/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.bench;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.*;
import org.ligoj.bootstrap.AbstractRestTest;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.springframework.test.annotation.Timed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD Integration REST tests.
 */
class CrudRestIT extends AbstractRestTest {

	/**
	 * URI
	 */
	private static final String FIND_ALL_PARAMETERS = "?draw=1&start=0&length=10&columns[0][data]=engine&order[0][column]=0&order[0][dir]=asc";
	private static final String RESOURCE = "/test/crud";

	private static final int NB_ITERATION = 100;

	/**
	 * Remote REST server.
	 */
	private static Server server;

	/**
	 * REST client.
	 */
	private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

	/**
	 * server creation.
	 */
	@BeforeAll
	static void startServer() {
		server = new CrudRestIT().startRestServer(null);
	}

	/**
	 * wine : test creation
	 */
	@Test
	void testCreate() throws IOException {
		create();
	}

	/**
	 * create a wine
	 */
	private int create() throws IOException {
		final var message = new HttpPost(BASE_URI + RESOURCE);
		message.addHeader("sm_universalid", DEFAULT_USER);
		message.setEntity(new StringEntity(
				"{\"name\":\"JUNIT" + "\",\"grapes\":\"Grenache / Syrah\"," + "\"country\":\"France\"," + "\"region\":\"Southern Rhone / Gigondas\","
						+ "\"year\":2009,\"picture\":\"saint_cosme.jpg\"," + "\"description\":\"The aromas of fruit ...\"}",
				ContentType.APPLICATION_JSON));
		return HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertTrue(NumberUtils.isDigits(content));
			return Integer.valueOf(content);
		});
	}

	/**
	 * Delete all wines
	 */
	private void deleteAll() throws IOException {
		final var message = new HttpDelete(BASE_URI + RESOURCE);
		message.addHeader("sm_universalid", DEFAULT_USER);
		HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, response.getCode());
			return null;
		});
	}

	/**
	 * wine : test read by id service
	 */
	@Test
	void testFindByUnknownId() throws IOException {
		final var message = new HttpGet(BASE_URI + RESOURCE + "/0");
		message.addHeader("sm_universalid", DEFAULT_USER);
		HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, response.getCode());
			return null;
		});
	}

	/**
	 * wine : test read by id service
	 */
	@Test
	void testFindById() throws IOException {
		testFindById(create());
	}

	/**
	 * wine : test read by id service
	 */
	private void testFindById(final int id) throws IOException {
		final var message = new HttpGet(BASE_URI + RESOURCE + "/" + id);
		message.addHeader("sm_universalid", DEFAULT_USER);
		HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertTrue(content.matches(".*\"id\":\"?" + id + ".*"));
			return null;
		});
	}

	/**
	 * wine : test update
	 */
	@Test
	void testUpdate() throws IOException {
		testUpdate(create());
	}

	/**
	 * update a wine
	 *
	 * @param id wine id
	 */
	private void testUpdate(final int id) throws IOException {
		final var message = new HttpPut(BASE_URI + RESOURCE);
		message.addHeader("sm_universalid", DEFAULT_USER);
		message.setEntity(new StringEntity("{\"id\":" + id + ",\"name\":\"JU" + id + "\",\"grapes\":\"Grenache / Syrah\"," + "\"country\":\"France\","
				+ "\"region\":\"Southern Rhone / Gigondas\"," + "\"year\":2009,\"picture\":\"saint_cosme.jpg\","
				+ "\"description\":\"The aromas of fruit ...\"}", ContentType.APPLICATION_JSON));
		HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, response.getCode());
			return null;
		});
	}

	/**
	 * test find all service
	 */
	@Test
	void testFindAll() throws IOException {
		testCreate();
		final var message = new HttpGet(BASE_URI + RESOURCE + FIND_ALL_PARAMETERS);
		message.addHeader("sm_universalid", DEFAULT_USER);
		HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());

			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("1", result.get("draw"));
			Assertions.assertTrue((Integer) result.get("recordsFiltered") > 0);
			Assertions.assertTrue((Integer) result.get("recordsTotal") > 0);
			@SuppressWarnings("unchecked") final var list = (List<Map<String, Object>>) result.get("data");
			Assertions.assertFalse(list.isEmpty());
			final var item = list.getFirst();
			Assertions.assertTrue((Integer) item.get("id") >= 0);
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("name")));
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("grapes")));
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("region")));
			Assertions.assertTrue((Integer) item.get("year") > 0);
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("picture")));
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("description")));
			return null;
		});
	}

	/**
	 * test find all service
	 */
	@Test
	void testFindAllQueryAlias() throws IOException {
		testCreate();
		final var message = new HttpGet(BASE_URI + RESOURCE + "/query/alias" + FIND_ALL_PARAMETERS);
		message.addHeader("sm_universalid", DEFAULT_USER);
		HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());

			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("1", result.get("draw"));
			Assertions.assertTrue((Integer) result.get("recordsFiltered") > 0);
			Assertions.assertTrue((Integer) result.get("recordsTotal") > 0);
			@SuppressWarnings("unchecked") final var list = (List<Map<String, Object>>) result.get("data");
			Assertions.assertFalse(list.isEmpty());
			final var item = list.getFirst();
			Assertions.assertTrue((Integer) item.get("id") >= 0);
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("name")));
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("grapes")));
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("region")));
			Assertions.assertTrue((Integer) item.get("year") > 0);
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("picture")));
			Assertions.assertTrue(StringUtils.isNotEmpty((CharSequence) item.get("description")));
			return null;
		});
	}

	/**
	 * wine : test delete
	 */
	@Test
	void testDelete() throws IOException {
		testDelete(create());
	}

	/**
	 * delete a wine
	 *
	 * @param id wine id
	 */
	private void testDelete(final int id) throws IOException {
		final var message = new HttpDelete(BASE_URI + RESOURCE + "/" + id);
		message.addHeader("sm_universalid", DEFAULT_USER);
		HTTP_CLIENT.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_NO_CONTENT, response.getCode());
			return null;
		});
	}

	/**
	 * test multiple call on create service
	 */
	@Test
	@Timed(millis = 4000)
	void testMultipleCreate() throws IOException {
		testCreateAll();
	}

	/**
	 * Initialize the data base
	 */
	private List<Integer> testCreateAll() throws IOException {
		final var identifiers = new ArrayList<Integer>();
		for (var i = NB_ITERATION; i-- > 0; ) {
			identifiers.add(create());
		}
		return identifiers;
	}

	/**
	 * test multiple call on update service
	 */
	@Test
	@Timed(millis = 4000)
	void testMultipleUpdate() throws IOException {
		for (final var id : testCreateAll()) {
			testUpdate(id);
		}
	}

	/**
	 * test multiple call on find all service
	 */
	@Test
	@Timed(millis = 8000)
	void testMultipleFindAll() throws IOException {
		testCreateAll();
		for (var loop = NB_ITERATION; loop-- > 0; ) {
			testFindAll();
		}
	}

	/**
	 * test multiple call on find by id service
	 */
	@Test
	@Timed(millis = 8000)
	void testMultipleFindById() throws IOException {
		for (final var id : testCreateAll()) {
			testFindById(id);
		}
	}

	/**
	 * test multiple call on delete service
	 */
	@Test
	@Timed(millis = 8000)
	void testMultipleDelete() throws IOException {
		for (final var id : testCreateAll()) {
			testDelete(id);
		}
	}

	/**
	 * Clean objects
	 */
	@BeforeEach
	void cleanup() throws Exception {
		deleteAll();
	}

	/**
	 * shutdown server
	 */
	@AfterAll
	static void tearDown() throws Exception {
		server.stop();
	}
}
