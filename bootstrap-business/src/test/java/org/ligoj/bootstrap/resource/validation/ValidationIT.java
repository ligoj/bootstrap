/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.validation;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test validation filter management with class {@link ValidationTestResource}.
 */
public class ValidationIT extends org.ligoj.bootstrap.AbstractRestTest {

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
		server = new ValidationIT().startRestServer("");
	}

	@Test
	void testValidationOk() throws IOException {
		final var httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.addHeader("sm_universalid", DEFAULT_USER);
		httppost.setEntity(new StringEntity(
				"{\"name\":\"JUNIT" + "\",\"grapes\":\"Grenache / Syrah\"," + "\"country\":\"France\"," + "\"region\":\"Southern Rhone / Gigondas\","
						+ "\"year\":2009,\"picture\":\"saint_cosme.jpg\"," + "\"description\":\"The aromas of fruit ...\"}",
				ContentType.APPLICATION_JSON));
		httpclient.execute(httppost, response -> {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());
			return null;
		});
	}

	/**
	 * Check the JAX-RS validation reject the null object in POST
	 */
	@Test
	void testValidationFilterFailedNull() throws IOException {
		final var httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.addHeader("sm_universalid", DEFAULT_USER);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(ACCEPT_LANGUAGE, "EN");
		httpclient.execute(httppost, response -> {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertNotNull(content);
			@SuppressWarnings("all") final var result = (Map<String, Map<String, List<Map<String, Object>>>>) new ObjectMapperTrim()
					.readValue(content, HashMap.class);

			Assertions.assertFalse(result.isEmpty());
			final var errors = result.get("errors");
			Assertions.assertNotNull(errors);
			Assertions.assertEquals(1, errors.size());
			Assertions.assertNotNull(errors.get("entity"));
			Assertions.assertEquals(1, ((Collection<?>) errors.get("entity")).size());
			Assertions.assertEquals(1, ((Map<?, ?>) ((List<?>) errors.get("entity")).get(0)).size());
			Assertions.assertEquals("NotNull", ((Map<?, ?>) ((List<?>) errors.get("entity")).get(0)).get(RULE));
			return null;
		});
	}

	@Test
	void testValidationFilterFailed() throws IOException {
		final var httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.addHeader("sm_universalid", DEFAULT_USER);
		httppost.setEntity(new StringEntity("{\"name\":\"" + "JunitJunitJunitJunitJunitJunitJunitJunitJunitJunit2\",\"year\":1000}",
				ContentType.APPLICATION_JSON));
		httppost.setHeader(ACCEPT_LANGUAGE, "EN");
		httpclient.execute(httppost, response -> {
			final var checkResponse = checkResponse(response);
			for (final var error : checkResponse) {
				if (error.get(RULE).equals("Length")) {
					final var parameters = (Map<?, ?>) error.get(PARAMETERS2);
					Assertions.assertEquals(2, parameters.size());
					Assertions.assertEquals(0, parameters.get("min"));
					Assertions.assertEquals(50, parameters.get("max"));
				} else if (error.get(RULE).equals("UpperCase")) {
					Assertions.assertNull(error.get(PARAMETERS2));
				} else {
					Assertions.fail("Unexpected error");
				}
			}
			return null;
		});
	}

	@Test
	void testInvalidFormatInteger() throws IOException {
		testInvalidFormat("year", "Integer");
	}

	private void testInvalidFormat(final String property, final String type) throws IOException {
		final var httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.addHeader("sm_universalid", DEFAULT_USER);
		httppost.setEntity(new StringEntity("{\"name\":\"" + "Junit2\",\"" + property + "\":\"A\"}", ContentType.APPLICATION_JSON));
		httppost.setHeader(ACCEPT_LANGUAGE, "EN");
		httpclient.execute(httppost, response -> {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertNotNull(content);
			@SuppressWarnings("all") final var result = (Map<String, Map<String, List<Map<String, Object>>>>) new ObjectMapperTrim()
					.readValue(content, HashMap.class);
			Assertions.assertFalse(result.isEmpty());
			final var errors = result.get("errors");
			Assertions.assertNotNull(errors);
			Assertions.assertEquals(1, errors.size());
			final var errorsOnYear = errors.get(property);
			Assertions.assertNotNull(errorsOnYear);
			Assertions.assertEquals(1, errorsOnYear.size());

			final var errorOnYear = errorsOnYear.get(0);
			Assertions.assertEquals(type, errorOnYear.get(RULE));
			Assertions.assertNull(errorOnYear.get(PARAMETERS2));
			return null;
		});
	}

	@Test
	void testValidationFilterFailedWithTarsOverriding() throws IOException {
		final var httppost = new HttpPost(BASE_URI + RESOURCE);
		httppost.setEntity(new StringEntity("{\"name\":\"" + "JunitJunitJunitJunitJunitJunitJunitJunitJunitJunit2\",\"year\":1000}",
				ContentType.APPLICATION_JSON));
		// Fix ATD-134
		httppost.addHeader("sm_universalid", DEFAULT_USER);
		httppost.setHeader(ACCEPT_LANGUAGE, "FR");
		httppost.setHeader("Accept-Charset", "utf-8");
		httpclient.execute(httppost, response -> {
			final var checkResponse = checkResponse(response);
			for (final var error : checkResponse) {
				if (error.get(RULE).equals("Length")) {
					final var parameters = (Map<?, ?>) error.get(PARAMETERS2);
					Assertions.assertEquals(2, parameters.size());
					Assertions.assertEquals(0, parameters.get("min"));
					Assertions.assertEquals(50, parameters.get("max"));
				} else if (error.get(RULE).equals("UpperCase")) {
					Assertions.assertNull(error.get(PARAMETERS2));
				} else {
					Assertions.fail("Unexpected error");
				}
			}
			return null;
		});
	}

	private List<Map<String, Object>> checkResponse(final ClassicHttpResponse response) throws IOException {
		Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
		final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
		Assertions.assertNotNull(content);
		@SuppressWarnings("all") final Map<String, Map<String, List<Map<String, Object>>>> result = (Map<String, Map<String, List<Map<String, Object>>>>) new ObjectMapperTrim()
				.readValue(content, HashMap.class);

		Assertions.assertFalse(result.isEmpty());
		final var errors = result.get("errors");
		Assertions.assertNotNull(errors);
		Assertions.assertFalse(errors.isEmpty());
		final var errorsOnName = errors.get("name");
		Assertions.assertNotNull(errorsOnName);
		Assertions.assertEquals(2, errorsOnName.size());
		Assertions.assertNotNull(errorsOnName.get(0));
		Assertions.assertNotNull(errorsOnName.get(1));
		final var errorsOnYear = errors.get("year");
		Assertions.assertNotNull(errorsOnYear);
		Assertions.assertEquals(1, errorsOnYear.size());
		Assertions.assertNotNull(errorsOnYear.get(0));
		return errorsOnName;
	}

	/**
	 * shutdown server
	 */
	@AfterAll
	public static void tearDown() throws Exception {
		server.stop();
	}
}
