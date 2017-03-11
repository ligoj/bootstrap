package org.ligoj.bootstrap.resource.system.bench;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Timed;

import org.ligoj.bootstrap.AbstractRestTest;
import org.ligoj.bootstrap.core.json.ObjectMapper;

/**
 * CRUD Integration REST tests.
 */
public class CrudRestTest extends AbstractRestTest {

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
	@BeforeClass
	public static void startServer() {
		server = new CrudRestTest().startRestServer("./src/test/resources/WEB-INF/web-test.xml");
	}

	/**
	 * wine : test creation
	 */
	@Test
	public void testCreate() throws IOException {
		create();
	}

	/**
	 * create a wine
	 * 
	 * @param id
	 *            wine id
	 */
	private int create() throws IOException {
		final HttpPost httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.setEntity(new StringEntity(
				"{\"name\":\"JUNIT" + "\",\"grapes\":\"Grenache / Syrah\"," + "\"country\":\"France\"," + "\"region\":\"Southern Rhone / Gigondas\","
						+ "\"year\":2009,\"picture\":\"saint_cosme.jpg\"," + "\"description\":\"The aromas of fruit ...\"}",
				ContentType.APPLICATION_JSON));
		final HttpResponse response = HTTP_CLIENT.execute(httppost);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertTrue(NumberUtils.isDigits(content));
			return Integer.valueOf(content);
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Delete all wines
	 */
	private void deleteAll() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE);
		final HttpResponse response = HTTP_CLIENT.execute(httpdelete);
		Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());
	}

	/**
	 * wine : test read by id service
	 */
	@Test
	public void testFindByUnknownId() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/0");
		final HttpResponse response = HTTP_CLIENT.execute(httpget);
		Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());
	}

	/**
	 * wine : test read by id service
	 */
	@Test
	public void testFindById() throws IOException {
		testFindById(create());
	}

	/**
	 * wine : test read by id service
	 */
	private void testFindById(final int id) throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/" + id);
		final HttpResponse response = HTTP_CLIENT.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertTrue(content.matches(".*\"id\":\"?" + id + ".*"));
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * wine : test update
	 */
	@Test
	public void testUpdate() throws IOException {
		testUpdate(create());
	}

	/**
	 * update a wine
	 * 
	 * @param id
	 *            wine id
	 */
	private void testUpdate(final int id) throws IOException {
		final HttpPut httpput = new HttpPut(BASE_URI + RESOURCE);
		httpput.setEntity(new StringEntity("{\"id\":" + id + ",\"name\":\"JU" + id + "\",\"grapes\":\"Grenache / Syrah\"," + "\"country\":\"France\","
				+ "\"region\":\"Southern Rhone / Gigondas\"," + "\"year\":2009,\"picture\":\"saint_cosme.jpg\","
				+ "\"description\":\"The aromas of fruit ...\"}", ContentType.APPLICATION_JSON));
		final HttpResponse response = HTTP_CLIENT.execute(httpput);
		Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());
	}

	/**
	 * test find all service
	 */
	@Test
	public void testFindAll() throws IOException {
		testCreate();
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + FIND_ALL_PARAMETERS);
		final HttpResponse response = HTTP_CLIENT.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapper().readValue(content, HashMap.class);
			Assert.assertEquals("1", result.get("draw"));
			Assert.assertTrue((Integer) result.get("recordsFiltered") > 0);
			Assert.assertTrue((Integer) result.get("recordsTotal") > 0);
			@SuppressWarnings("unchecked")
			final List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("data");
			Assert.assertFalse(list.isEmpty());
			final Map<String, Object> item = list.get(0);
			Assert.assertTrue((Integer) item.get("id") >= 0);
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("name")));
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("grapes")));
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("region")));
			Assert.assertTrue((Integer) item.get("year") > 0);
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("picture")));
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("description")));
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * test find all service
	 */
	@Test
	public void testFindAllQueryAlias() throws IOException {
		testCreate();
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/query/alias" + FIND_ALL_PARAMETERS);
		final HttpResponse response = HTTP_CLIENT.execute(httpget);
		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapper().readValue(content, HashMap.class);
			Assert.assertEquals("1", result.get("draw"));
			Assert.assertTrue((Integer) result.get("recordsFiltered") > 0);
			Assert.assertTrue((Integer) result.get("recordsTotal") > 0);
			@SuppressWarnings("unchecked")
			final List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("data");
			Assert.assertFalse(list.isEmpty());
			final Map<String, Object> item = list.get(0);
			Assert.assertTrue((Integer) item.get("id") >= 0);
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("name")));
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("grapes")));
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("region")));
			Assert.assertTrue((Integer) item.get("year") > 0);
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("picture")));
			Assert.assertTrue(StringUtils.isNotEmpty((String) item.get("description")));
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * wine : test delete
	 */
	@Test
	public void testDelete() throws IOException {
		testDelete(create());
	}

	/**
	 * delete a wine
	 * 
	 * @param id
	 *            wine id
	 */
	private void testDelete(final int id) throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/" + id);
		final HttpResponse response = HTTP_CLIENT.execute(httpdelete);
		try {
			Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusLine().getStatusCode());
		} finally {
			if (response.getEntity() != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * test multiple call on create service
	 */
	@Test
	@Timed(millis = 4000)
	public void testMultipleCreate() throws IOException {
		testCreateAll();
	}

	/**
	 * Initialize the data base
	 */
	private List<Integer> testCreateAll() throws IOException {
		final List<Integer> identifiers = new ArrayList<>();
		for (int i = NB_ITERATION; i-- > 0;) {
			identifiers.add(create());
		}
		return identifiers;
	}

	/**
	 * test multiple call on update service
	 */
	@Test
	@Timed(millis = 4000)
	public void testMultipleUpdate() throws IOException {
		for (final int id : testCreateAll()) {
			testUpdate(id);
		}
	}

	/**
	 * test multiple call on find all service
	 */
	@Test
	@Timed(millis = 8000)
	public void testMultipleFindAll() throws IOException {
		testCreateAll();
		for (int loop = NB_ITERATION; loop-- > 0;) {
			testFindAll();
		}
	}

	/**
	 * test multiple call on find by id service
	 */
	@Test
	@Timed(millis = 8000)
	public void testMultipleFindById() throws IOException {
		for (final int id : testCreateAll()) {
			testFindById(id);
		}
	}

	/**
	 * test multiple call on delete service
	 */
	@Test
	@Timed(millis = 8000)
	public void testMultipleDelete() throws IOException {
		for (final int id : testCreateAll()) {
			testDelete(id);
		}
	}

	/**
	 * Clean objects
	 */
	@Before
	public void cleanup() throws Exception {
		deleteAll();
	}

	/**
	 * shutdown server
	 */
	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
	}
}
