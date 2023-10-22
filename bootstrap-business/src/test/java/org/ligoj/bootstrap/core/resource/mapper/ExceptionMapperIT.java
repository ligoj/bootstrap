/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.BusinessException;
import org.ligoj.bootstrap.model.test.Wine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception mapper test using {@link ExceptionMapperResource}
 */
public class ExceptionMapperIT extends org.ligoj.bootstrap.AbstractRestTest {

	/**
	 * URI
	 */
	private static final String RESOURCE = "/throw";

	/**
	 * Remote REST server.
	 */
	private static Server server;

	/**
	 * server creation.
	 */
	@BeforeAll
	public static void startServer() {
		server = new ExceptionMapperIT().startRestServer("");
	}

	/**
	 * shutdown server
	 */
	@AfterAll
	public static void tearDown() throws Exception {
		if (server != null) {
			server.stop();
		}
	}
	/**
	 * @see ExceptionMapperResource#throwFailSafe()
	 */
	@Test
	void testInternalError() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/failsafe");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwFailSafe2()
	 */
	@Test
	void testInternalError2() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/failsafe2");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwFailSafe2()
	 */
	@Test
	void testInternalError3() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/failsafe3");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwDataIntegrityException()
	 */
	@Test
	void testIntegrityForeignError() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/integrity-foreign");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("integrity-foreign", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("assignment/project", result.get("message"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwDataIntegrityUnicityException()
	 */
	@Test
	void testIntegrityUnicityError() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/integrity-unicity");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("integrity-unicity", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("2003/PRIMARY", result.get("message"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwDataIntegrityUnknownException()
	 */
	@Test
	void testIntegrityUnknownError() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/integrity-unknown");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("integrity-unknown", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("Any SQL error", result.get("message"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwTransaction()
	 */
	@Test
	void testTransactionError() throws IOException {
		assertUnavailable("/transaction-begin");
	}

	/**
	 * @see ExceptionMapperResource#throwTransaction()
	 */
	@Test
	void testConnectionError() throws IOException {
		assertUnavailable("/connection");
	}

	/**
	 * @see ExceptionMapperResource#throwTransaction2()
	 */
	@Test
	void testTransactionError2() throws IOException {
		assertUnavailable("/transaction-begin2");
	}

	private void assertUnavailable(final String path) throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + path);
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNull(result.get("message"));
			Assertions.assertEquals("database-down", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwCommunicationException()
	 */
	@Test
	void testCommunicationException() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/ldap");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("Connection refused", result.get("message"));
			Assertions.assertEquals("ldap-down", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwMailSendException()
	 */
	@Test
	void testMailSendException() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/mail");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNull(result.get("message"));
			Assertions.assertEquals("mail-down", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwTechnical()
	 */
	@Test
	void testTechnicalErrorWithCause() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/technical");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("technical", result.get("code"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwBusiness()
	 */
	@Test
	void testBusinessError() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/business");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals(BusinessException.KEY_UNKNOWN_ID, result.get("message"));
			Assertions.assertEquals("business", result.get("code"));
			Assertions.assertNull(result.get("cause"));

			@SuppressWarnings("unchecked") final List<Object> parameters = (List<Object>) result.get("parameters");
			Assertions.assertNotNull(parameters);
			Assertions.assertEquals(2, parameters.size());
			Assertions.assertEquals("parameter1", parameters.get(0));
			Assertions.assertEquals("parameter2", parameters.get(1));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwWebApplication()
	 */
	@Test
	void testJaxRSError() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/jax-rs");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwJSonMapping()
	 */
	@Test
	void testJSonMappingError() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/json-mapping");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertEquals("{errors={dialDouble=[{rule=Double}]}}", new ObjectMapperTrim().readValue(content, HashMap.class).toString());
			return content;
		});
	}

	@Test
	void testJaxRS404Error() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/unknown");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwWebApplication()
	 */
	@Test
	void testJaxRS405Error() throws IOException {
		final var httpget = new HttpGet(BASE_URI + RESOURCE + "/jax-rs");
		httpclient.execute(httpget, response -> {
			Assertions.assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#hook(String, String, Wine)
	 */
	@Test
	void testHook() throws IOException, InterruptedException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/hook/p1/p2");
		httpdelete.setEntity(new StringEntity("{\"name\":\"JUNIT\"}", ContentType.APPLICATION_JSON));
		httpdelete.setHeader("SM_UNIVERSALID", DEFAULT_USER);
		httpclient.execute(httpdelete, response -> {
			try {
				// Wait for async execution
				Thread.sleep(2000);
			} catch (final Exception e) {
				// Ignore
			}
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());
			return null;
		});
		Thread.sleep(2000);
	}

	/**
	 * @see ExceptionMapperResource#throwAccessDeniedException()
	 */
	@Test
	void testAccessDenied() throws IOException {
		assertForbidden("/security-403");
	}

	/**
	 * @see ExceptionMapperResource#throwForbiddenException()
	 */
	@Test
	void testForbiddenException() throws IOException {
		assertForbidden("/security-403-rs");
	}

	private void assertForbidden(final String path) throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + path);
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("security", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertNull(result.get("message"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwAuthenticationException()
	 */
	@Test
	void testAuthenticationException() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/security-401");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("security", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("message", result.get("message"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwNotImplemented()
	 */
	@Test
	void notImplemented() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/not-implemented");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("not-implemented", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("message", result.get("message"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwJsr303()
	 */
	@Test
	void testJSR303() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/jsr-303");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNotNull(result.get("errors"));
			Assertions.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked") final Map<?, ?> errors = (Map<String, Object>) result.get("errors");
			Assertions.assertEquals(1, errors.size());
			Assertions.assertNotNull(errors.get("jsr303"));
			Assertions.assertTrue(errors.get("jsr303") instanceof List);
			@SuppressWarnings("unchecked") final List<Map<String, String>> rules = (List<Map<String, String>>) errors.get("jsr303");
			Assertions.assertEquals(1, rules.size());
			Assertions.assertNotNull(rules.get(0));
			Assertions.assertEquals(1, rules.get(0).size());
			Assertions.assertNotNull(rules.get(0).get("rule"));
			Assertions.assertEquals("NotNull", rules.get(0).get("rule"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwJsr303FromJpa()
	 */
	@Test
	void testJSR303Jpa() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/jsr-303-jpa");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNotNull(result.get("errors"));
			Assertions.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked") final var errors = (Map<String, Object>) result.get("errors");
			Assertions.assertEquals(1, errors.size());
			Assertions.assertNotNull(errors.get("name"));
			Assertions.assertTrue(errors.get("name") instanceof List);
			@SuppressWarnings("unchecked") final List<Map<String, ?>> rules = (List<Map<String, ?>>) errors.get("name");
			Assertions.assertEquals(1, rules.size());
			Assertions.assertNotNull(rules.get(0));
			Assertions.assertEquals(2, rules.get(0).size());
			Assertions.assertNotNull(rules.get(0).get("rule"));
			Assertions.assertEquals("Length", rules.get(0).get("rule"));
			@SuppressWarnings("unchecked") final Map<String, Integer> parameters = (Map<String, Integer>) rules.get(0).get("parameters");
			Assertions.assertEquals(0, parameters.get("min").intValue());
			Assertions.assertEquals(200, parameters.get("max").intValue());
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwTransactionSystemException()
	 */
	@Test
	void testUnknownTransactionalException() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/transaction-commit");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("message", result.get("message"));
			Assertions.assertEquals("technical", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwUnrecognizedPropertyException(org.ligoj.bootstrap.model.system.SystemUser)
	 */
	@Test
	void testUnrecognizedPropertyException() throws IOException {
		final var httppost = new HttpPost(BASE_URI + RESOURCE + "/unrecognized-property");
		httppost.setEntity(new StringEntity("{\"login\":\"JUNIT" + "\",\"any\":\"Grenache / Syrah\"}", ContentType.APPLICATION_JSON));
		httpclient.execute(httppost, response -> {
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNotNull(result.get("errors"));
			Assertions.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked") final var errors = (Map<String, Object>) result.get("errors");
			Assertions.assertEquals(1, errors.size());
			Assertions.assertNotNull(errors.get("any"));
			Assertions.assertTrue(errors.get("any") instanceof List);
			@SuppressWarnings("unchecked") final List<Map<String, String>> rules = (List<Map<String, String>>) errors.get("any");
			Assertions.assertEquals(1, rules.size());
			Assertions.assertNotNull(rules.get(0));
			Assertions.assertEquals(1, rules.get(0).size());
			Assertions.assertNotNull(rules.get(0).get("rule"));
			Assertions.assertEquals("Mapping", rules.get(0).get("rule"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwEntityNotFoundException()
	 */
	@Test
	void testEntityNotFoundException() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/entityNotFoundException");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("entity", result.get("code"));
			Assertions.assertEquals("key", result.get("message"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwCannotAcquireLockException()
	 */
	@Test
	void testCannotAcquireLockException() throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/cannotAcquireLockException");
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_CONFLICT, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("database-lock", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

	/**
	 * @see ExceptionMapperResource#throwJpaObjectRetrievalFailureException()
	 */
	@Test
	void testJpaObjectRetrievalFailureException() throws IOException {
		assertNotFound("/jpaObjectRetrievalFailureException", "key");
	}

	/**
	 * @see ExceptionMapperResource#throwNoResultException()
	 */
	@Test
	void testNoResultException() throws IOException {
		assertNotFound("/noResultException", "message");
	}

	private void assertNotFound(final String path, final String message) throws IOException {
		final var httpdelete = new HttpDelete(BASE_URI + RESOURCE + path);
		httpclient.execute(httpdelete, response -> {
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getCode());
			final var content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final var result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("entity", result.get("code"));
			Assertions.assertEquals(message, result.get("message"));
			Assertions.assertNull(result.get("cause"));
			return content;
		});
	}

}
