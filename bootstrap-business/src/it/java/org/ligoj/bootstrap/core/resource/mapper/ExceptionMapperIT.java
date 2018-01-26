package org.ligoj.bootstrap.core.resource.mapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.ligoj.bootstrap.AbstractRestTest;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.BusinessException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Exception mapper test using {@link ExceptionMapperResource}
 */
public class ExceptionMapperIT extends AbstractRestTest {

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
		server = new ExceptionMapperIT().startRestServer("./src/test/resources/WEB-INF/web-test-nosecurity.xml");
	}

	/**
	 * @see ExceptionMapperResource#throwFailSafe()
	 */
	@Test
	public void testInternalError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/failsafe");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwFailSafe2()
	 */
	@Test
	public void testInternalError2() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/failsafe2");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwFailSafe2()
	 */
	@Test
	public void testInternalError3() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/failsafe3");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwDataIntegrityException()
	 */
	@Test
	public void testIntegrityForeignError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/integrity-foreign");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("integrity-foreign", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("assignment/project", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwDataIntegrityUnicityException()
	 */
	@Test
	public void testIntegrityUnicityError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/integrity-unicity");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("integrity-unicity", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("2003/PRIMARY", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwDataIntegrityUnknownException()
	 */
	@Test
	public void testIntegrityUnknownError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/integrity-unknown");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("integrity-unknown", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("Any SQL error", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwTransaction()
	 */
	@Test
	public void testTransactionError() throws IOException {
		assertUnavailable("/transaction-begin");
	}

	/**
	 * @see ExceptionMapperResource#throwTransaction()
	 */
	@Test
	public void testConnectionError() throws IOException {
		assertUnavailable("/connection");
	}

	/**
	 * @see ExceptionMapperResource#throwTransaction2()
	 */
	@Test
	public void testTransactionError2() throws IOException {
		assertUnavailable("/transaction-begin2");
	}

	private void assertUnavailable(final String path) throws IOException, ClientProtocolException, JsonParseException, JsonMappingException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + path);
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNull(result.get("message"));
			Assertions.assertEquals("database-down", result.get("code"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwCommunicationException()
	 */
	@Test
	public void testCommunicationException() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/ldap");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("Connection refused", result.get("message"));
			Assertions.assertEquals("ldap-down", result.get("code"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwMailSendException()
	 */
	@Test
	public void testMailSendException() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/mail");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNull(result.get("message"));
			Assertions.assertEquals("mail-down", result.get("code"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwTechnical()
	 */
	@Test
	public void testTechnicalErrorWithCause() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/technical");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("message", result.get("message"));
			Assertions.assertEquals("technical", result.get("code"));
			Assertions.assertNotNull(result.get("cause"));
			@SuppressWarnings("unchecked")
			final Map<?, ?> cause = (Map<String, String>) result.get("cause");
			Assertions.assertEquals("message", cause.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwBusiness()
	 */
	@Test
	public void testBusinessError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/business");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals(BusinessException.KEY_UNKNOW_ID, result.get("message"));
			Assertions.assertEquals("business", result.get("code"));
			Assertions.assertNull(result.get("cause"));

			@SuppressWarnings("unchecked")
			final List<Object> parameters = (List<Object>) result.get("parameters");
			Assertions.assertNotNull(parameters);
			Assertions.assertEquals(2, parameters.size());
			Assertions.assertEquals("parameter1", parameters.get(0));
			Assertions.assertEquals("parameter2", parameters.get(1));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwWebApplication()
	 */
	@Test
	public void testJaxRSError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/jax-rs");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("HTTP 500 Internal Server Error", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwJSonMapping()
	 */
	@Test
	public void testJSonMappingError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/json-mapping");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assertions.assertEquals("{errors={dialDouble=[{rule=Double}]}}", new ObjectMapperTrim().readValue(content, HashMap.class).toString());
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	@Test
	public void testJaxRS404Error() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/unknow");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("HTTP 404 Not Found", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwWebApplication()
	 */
	@Test
	public void testJaxRS405Error() throws IOException {
		final HttpGet httpget = new HttpGet(BASE_URI + RESOURCE + "/jax-rs");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
			Assertions.assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("internal", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("HTTP 405 Method Not Allowed", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwAccessDeniedException()
	 */
	@Test
	public void testAccessDenied() throws IOException {
		assertForbidden("/security-403");
	}

	/**
	 * @see ExceptionMapperResource#throwForbiddenException()
	 */
	@Test
	public void testForbiddenException() throws IOException {
		assertForbidden("/security-403-rs");
	}

	private void assertForbidden(final String path) throws IOException, ClientProtocolException, JsonParseException, JsonMappingException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + path);
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("security", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertNull(result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwAuthenticationException()
	 */
	@Test
	public void testAuthenticationException() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/security-401");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("security", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("message", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwNotImplemented()
	 */
	@Test
	public void notImplemented() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/not-implemented");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("not-implemented", result.get("code"));
			Assertions.assertNull(result.get("cause"));
			Assertions.assertEquals("message", result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwJsr303()
	 */
	@Test
	public void testJSR303() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/jsr-303");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNotNull(result.get("errors"));
			Assertions.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked")
			final Map<?, ?> errors = (Map<String, Object>) result.get("errors");
			Assertions.assertEquals(1, errors.size());
			Assertions.assertNotNull(errors.get("jsr303"));
			Assertions.assertTrue(errors.get("jsr303") instanceof List);
			@SuppressWarnings("unchecked")
			final List<Map<String, String>> rules = (List<Map<String, String>>) errors.get("jsr303");
			Assertions.assertEquals(1, rules.size());
			Assertions.assertNotNull(rules.get(0));
			Assertions.assertEquals(1, rules.get(0).size());
			Assertions.assertNotNull(rules.get(0).get("rule"));
			Assertions.assertEquals("NotNull", rules.get(0).get("rule"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwJsr303FromJpa()
	 */
	@Test
	public void testJSR303Jpa() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/jsr-303-jpa");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNotNull(result.get("errors"));
			Assertions.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked")
			final Map<?, ?> errors = (Map<String, Object>) result.get("errors");
			Assertions.assertEquals(1, errors.size());
			Assertions.assertNotNull(errors.get("name"));
			Assertions.assertTrue(errors.get("name") instanceof List);
			@SuppressWarnings("unchecked")
			final List<Map<String, ?>> rules = (List<Map<String, ?>>) errors.get("name");
			Assertions.assertEquals(1, rules.size());
			Assertions.assertNotNull(rules.get(0));
			Assertions.assertEquals(2, rules.get(0).size());
			Assertions.assertNotNull(rules.get(0).get("rule"));
			Assertions.assertEquals("Length", rules.get(0).get("rule"));
			@SuppressWarnings("unchecked")
			final Map<String, Integer> parameters = (Map<String, Integer>) rules.get(0).get("parameters");
			Assertions.assertEquals(0, parameters.get("min").intValue());
			Assertions.assertEquals(200, parameters.get("max").intValue());
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwTransactionSystemException()
	 */
	@Test
	public void testUnknownTransactionnalException() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/transaction-commit");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("message", result.get("message"));
			Assertions.assertEquals("technical", result.get("code"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwUnrecognizedPropertyException(org.ligoj.bootstrap.model.system.SystemUser)
	 */
	@Test
	public void testUnrecognizedPropertyException() throws IOException {
		final HttpPost httppost = new HttpPost(BASE_URI + RESOURCE + "/unrecognized-property");
		HttpResponse response = null;
		try {
			httppost.setEntity(new StringEntity("{\"login\":\"JUNIT" + "\",\"any\":\"Grenache / Syrah\"}", ContentType.APPLICATION_JSON));
			response = httpclient.execute(httppost);
			Assertions.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertNotNull(result.get("errors"));
			Assertions.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked")
			final Map<?, ?> errors = (Map<String, Object>) result.get("errors");
			Assertions.assertEquals(1, errors.size());
			Assertions.assertNotNull(errors.get("any"));
			Assertions.assertTrue(errors.get("any") instanceof List);
			@SuppressWarnings("unchecked")
			final List<Map<String, String>> rules = (List<Map<String, String>>) errors.get("any");
			Assertions.assertEquals(1, rules.size());
			Assertions.assertNotNull(rules.get(0));
			Assertions.assertEquals(1, rules.get(0).size());
			Assertions.assertNotNull(rules.get(0).get("rule"));
			Assertions.assertEquals("Mapping", rules.get(0).get("rule"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwEntityNotFoundException()
	 */
	@Test
	public void testEntityNotFoundException() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/entityNotFoundException");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("entity", result.get("code"));
			Assertions.assertEquals("key", result.get("message"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwCannotAcquireLockException()
	 */
	@Test
	public void testCannotAcquireLockException() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/cannotAcquireLockException");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_CONFLICT, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("database-lock", result.get("code"));
			Assertions.assertNull(result.get("message"));
			Assertions.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwJpaObjectRetrievalFailureException()
	 */
	@Test
	public void testJpaObjectRetrievalFailureException() throws IOException {
		assertNotFound("/jpaObjectRetrievalFailureException","key");
	}

	/**
	 * @see ExceptionMapperResource#throwNoResultException()
	 */
	@Test
	public void testNoResultException() throws IOException {
		assertNotFound("/noResultException", "message");
	}

	private void assertNotFound(final String path, final String message) throws IOException, ClientProtocolException, JsonParseException, JsonMappingException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + path);
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assertions.assertEquals("entity", result.get("code"));
			Assertions.assertEquals(message, result.get("message"));
			Assertions.assertNull(result.get("cause"));
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
