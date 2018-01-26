package org.ligoj.bootstrap.core.validation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.AbstractRestTest;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.extern.slf4j.Slf4j;

/**
 * Test validation filter management.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
@Slf4j
public class ValidationJSonIT extends AbstractRestTest {

	private static final String RULE = "rule";

	private static final String PARAMETERS2 = "parameters";

	private static final String ACCEPT_LANGUAGE = "Accept-Language";

	/**
	 * URI
	 */
	private static final String RESOURCE = "/test/validation";

	/**
	 * Remote REST server.
	 */
	private static Server server;

	/**
	 * server creation.
	 */
	@BeforeAll
	public static void startServer() {
		server = new ValidationJSonIT().startRestServer("./src/test/resources/WEB-INF/web-test-nosecurity.xml");
	}

	@Test
	public void testValidationOk() throws IOException {
		final HttpPost httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.setEntity(new StringEntity(
				"{\"name\":\"JUNIT" + "\",\"grapes\":\"Grenache / Syrah\"," + "\"country\":\"France\"," + "\"region\":\"Southern Rhone / Gigondas\","
						+ "\"year\":2009,\"picture\":\"saint_cosme.jpg\"," + "\"description\":\"The aromas of fruit ...\"}",
				ContentType.APPLICATION_JSON));
		final HttpResponse response = httpclient.execute(httppost);
		try {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		} finally {
			response.getEntity().getContent().close();
		}
	}

	/**
	 * Check the JAX-RS validation reject the null object in POST
	 */
	@Test
	public void testValidationFilterFailedNull() throws IOException {
		final HttpPost httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(ACCEPT_LANGUAGE, "EN");
		final HttpResponse response = httpclient.execute(httppost);
		Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
		final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
		Assertions.assertNotNull(content);
		@SuppressWarnings("all")
		final Map<String, Map<String, List<Map<String, Object>>>> result = (Map<String, Map<String, List<Map<String, Object>>>>) new ObjectMapperTrim()
				.readValue(content, HashMap.class);

		Assertions.assertFalse(result.isEmpty());
		final Map<String, List<Map<String, Object>>> errors = result.get("errors");
		Assertions.assertNotNull(errors);
		Assertions.assertEquals(1, errors.size());
		log.info("### ENTRY ####" + errors.keySet().iterator().next());
		Assertions.assertNotNull(errors.get("wine"));
		Assertions.assertEquals(1, ((List<?>) errors.get("wine")).size());
		Assertions.assertEquals(1, ((Map<?, ?>) ((List<?>) errors.get("wine")).get(0)).size());
		Assertions.assertEquals(((Map<?, ?>) ((List<?>) errors.get("wine")).get(0)).get(RULE), "NotNull");
	}

	@Test
	public void testValidationFilterFailed() throws IOException {
		final HttpPost httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.setEntity(new StringEntity("{\"name\":\"" + "JunitJunitJunitJunitJunitJunitJunitJunitJunitJunit2\",\"year\":1000}",
				ContentType.APPLICATION_JSON));
		httppost.setHeader(ACCEPT_LANGUAGE, "EN");
		final HttpResponse response = httpclient.execute(httppost);
		final List<Map<String, Object>> checkResponse = checkResponse(response);
		for (final Map<String, Object> error : checkResponse) {
			if (error.get(RULE).equals("Length")) {
				final Map<?, ?> parameters = (Map<?, ?>) error.get(PARAMETERS2);
				Assertions.assertEquals(2, parameters.size());
				Assertions.assertEquals(0, parameters.get("min"));
				Assertions.assertEquals(50, parameters.get("max"));
			} else if (error.get(RULE).equals("UpperCase")) {
				Assertions.assertNull(error.get(PARAMETERS2));
			} else {
				Assertions.fail("Unexpected error");
			}
		}
	}

	@Test
	public void testInvalidFormatInteger() throws IOException {
		testInvalidFormat("year", "Integer");
	}

	@Test
	public void testInvalidFormatDate() throws IOException {
		testInvalidFormat("date", "Date");
	}

	private void testInvalidFormat(final String property,final String type) throws IOException, ClientProtocolException, JsonParseException, JsonMappingException {
		final HttpPost httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.setEntity(new StringEntity("{\"name\":\"" + "Junit2\",\""+property+"\":\"A\"}", ContentType.APPLICATION_JSON));
		httppost.setHeader(ACCEPT_LANGUAGE, "EN");
		final HttpResponse response = httpclient.execute(httppost);
		try {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertNotNull(content);
			@SuppressWarnings("all")
			final Map<String, Map<String, List<Map<String, Object>>>> result = (Map<String, Map<String, List<Map<String, Object>>>>) new ObjectMapperTrim()
					.readValue(content, HashMap.class);
			Assertions.assertFalse(result.isEmpty());
			final Map<String, List<Map<String, Object>>> errors = result.get("errors");
			Assertions.assertNotNull(errors);
			Assertions.assertEquals(1, errors.size());
			final List<Map<String, Object>> errorsOnYear = errors.get(property);
			Assertions.assertNotNull(errorsOnYear);
			Assertions.assertEquals(1, errorsOnYear.size());

			final Map<String, Object> errorOnYear = errorsOnYear.get(0);
			Assertions.assertEquals(type, errorOnYear.get(RULE));
			Assertions.assertNull(errorOnYear.get(PARAMETERS2));
		} finally {
			response.getEntity().getContent().close();
		}
	}

	@Test
	public void testValidationFilterFailedWithTarsOverriding() throws IOException {
		final HttpPost httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.setEntity(new StringEntity("{\"name\":\"" + "JunitJunitJunitJunitJunitJunitJunitJunitJunitJunit2\",\"year\":1000}",
				ContentType.APPLICATION_JSON));
		// Fix ATD-134
		httppost.setHeader(ACCEPT_LANGUAGE, "FR");
		httppost.setHeader("Accept-Charset", "utf-8");
		final HttpResponse response = httpclient.execute(httppost);
		final List<Map<String, Object>> checkResponse = checkResponse(response);
		for (final Map<String, Object> error : checkResponse) {
			if (error.get(RULE).equals("Length")) {
				final Map<?, ?> parameters = (Map<?, ?>) error.get(PARAMETERS2);
				Assertions.assertEquals(2, parameters.size());
				Assertions.assertEquals(0, parameters.get("min"));
				Assertions.assertEquals(50, parameters.get("max"));
			} else if (error.get(RULE).equals("UpperCase")) {
				Assertions.assertNull(error.get(PARAMETERS2));
			} else {
				Assertions.fail("Unexpected error");
			}
		}
	}

	private List<Map<String, Object>> checkResponse(final HttpResponse response) throws IOException {
		try {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertNotNull(content);
			@SuppressWarnings("all")
			final Map<String, Map<String, List<Map<String, Object>>>> result = (Map<String, Map<String, List<Map<String, Object>>>>) new ObjectMapperTrim()
					.readValue(content, HashMap.class);

			Assertions.assertFalse(result.isEmpty());
			final Map<String, List<Map<String, Object>>> errors = result.get("errors");
			Assertions.assertNotNull(errors);
			Assertions.assertFalse(errors.isEmpty());
			final List<Map<String, Object>> errorsOnName = errors.get("name");
			Assertions.assertNotNull(errorsOnName);
			Assertions.assertEquals(2, errorsOnName.size());
			Assertions.assertNotNull(errorsOnName.get(0));
			Assertions.assertNotNull(errorsOnName.get(1));
			final List<Map<String, Object>> errorsOnYear = errors.get("year");
			Assertions.assertNotNull(errorsOnYear);
			Assertions.assertEquals(1, errorsOnYear.size());
			Assertions.assertNotNull(errorsOnYear.get(0));
			return errorsOnName;
		} finally {
			response.getEntity().getContent().close();
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
