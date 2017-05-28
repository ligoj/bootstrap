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
	@BeforeClass
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
			Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("internal", result.get("code"));
			Assert.assertNull(result.get("message"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("internal", result.get("code"));
			Assert.assertNull(result.get("message"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("internal", result.get("code"));
			Assert.assertNull(result.get("message"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("integrity-foreign", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("assignment/project", result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("integrity-unicity", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("2003/PRIMARY", result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_PRECONDITION_FAILED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("integrity-unknown", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("Any SQL error", result.get("message"));
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
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/transaction-begin");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assert.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("database-down", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertNull(result.get("message"));
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
	public void testConnectionError() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/connection");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assert.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("database-down", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertNull(result.get("message"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwTransaction2()
	 */
	@Test
	public void testTransactionError2() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/transaction-begin2");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assert.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertNull(result.get("message"));
			Assert.assertEquals("database-down", result.get("code"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("Connection refused", result.get("message"));
			Assert.assertEquals("ldap-down", result.get("code"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_SERVICE_UNAVAILABLE, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertNull(result.get("message"));
			Assert.assertEquals("mail-down", result.get("code"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("message", result.get("message"));
			Assert.assertEquals("technical", result.get("code"));
			Assert.assertNotNull(result.get("cause"));
			@SuppressWarnings("unchecked")
			final Map<?, ?> cause = (Map<String, String>) result.get("cause");
			Assert.assertEquals("message", cause.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals(BusinessException.KEY_UNKNOW_ID, result.get("message"));
			Assert.assertEquals("business", result.get("code"));
			Assert.assertNull(result.get("cause"));

			@SuppressWarnings("unchecked")
			final List<Object> parameters = (List<Object>) result.get("parameters");
			Assert.assertNotNull(parameters);
			Assert.assertEquals(2, parameters.size());
			Assert.assertEquals("parameter1", parameters.get(0));
			Assert.assertEquals("parameter2", parameters.get(1));
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
			Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("internal", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("HTTP 500 Internal Server Error", result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			Assert.assertEquals("{errors={dialDouble=[{rule=Double}]}}", new ObjectMapperTrim().readValue(content, HashMap.class).toString());
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
			Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("internal", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("HTTP 404 Not Found", result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("internal", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("HTTP 405 Method Not Allowed", result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("security", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertNull(result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("security", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("message", result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("not-implemented", result.get("code"));
			Assert.assertNull(result.get("cause"));
			Assert.assertEquals("message", result.get("message"));
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
			Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertNotNull(result.get("errors"));
			Assert.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked")
			final Map<?, ?> errors = (Map<String, Object>) result.get("errors");
			Assert.assertEquals(1, errors.size());
			Assert.assertNotNull(errors.get("jsr303"));
			Assert.assertTrue(errors.get("jsr303") instanceof List);
			@SuppressWarnings("unchecked")
			final List<Map<String, String>> rules = (List<Map<String, String>>) errors.get("jsr303");
			Assert.assertEquals(1, rules.size());
			Assert.assertNotNull(rules.get(0));
			Assert.assertEquals(1, rules.get(0).size());
			Assert.assertNotNull(rules.get(0).get("rule"));
			Assert.assertEquals("NotNull", rules.get(0).get("rule"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertNotNull(result.get("errors"));
			Assert.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked")
			final Map<?, ?> errors = (Map<String, Object>) result.get("errors");
			Assert.assertEquals(1, errors.size());
			Assert.assertNotNull(errors.get("name"));
			Assert.assertTrue(errors.get("name") instanceof List);
			@SuppressWarnings("unchecked")
			final List<Map<String, ?>> rules = (List<Map<String, ?>>) errors.get("name");
			Assert.assertEquals(1, rules.size());
			Assert.assertNotNull(rules.get(0));
			Assert.assertEquals(2, rules.get(0).size());
			Assert.assertNotNull(rules.get(0).get("rule"));
			Assert.assertEquals("Length", rules.get(0).get("rule"));
			@SuppressWarnings("unchecked")
			final Map<String, Integer> parameters = (Map<String, Integer>) rules.get(0).get("parameters");
			Assert.assertEquals(0, parameters.get("min").intValue());
			Assert.assertEquals(200, parameters.get("max").intValue());
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("message", result.get("message"));
			Assert.assertEquals("technical", result.get("code"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertNotNull(result.get("errors"));
			Assert.assertTrue(result.get("errors") instanceof Map<?, ?>);
			@SuppressWarnings("unchecked")
			final Map<?, ?> errors = (Map<String, Object>) result.get("errors");
			Assert.assertEquals(1, errors.size());
			Assert.assertNotNull(errors.get("any"));
			Assert.assertTrue(errors.get("any") instanceof List);
			@SuppressWarnings("unchecked")
			final List<Map<String, String>> rules = (List<Map<String, String>>) errors.get("any");
			Assert.assertEquals(1, rules.size());
			Assert.assertNotNull(rules.get(0));
			Assert.assertEquals(1, rules.get(0).size());
			Assert.assertNotNull(rules.get(0).get("rule"));
			Assert.assertEquals("Mapping", rules.get(0).get("rule"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("entity", result.get("code"));
			Assert.assertEquals("key", result.get("message"));
			Assert.assertNull(result.get("cause"));
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
			Assert.assertEquals(HttpStatus.SC_CONFLICT, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("database-lock", result.get("code"));
			Assert.assertNull(result.get("message"));
			Assert.assertNull(result.get("cause"));
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
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/jpaObjectRetrievalFailureException");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("entity", result.get("code"));
			Assert.assertEquals("key", result.get("message"));
			Assert.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
		}
	}

	/**
	 * @see ExceptionMapperResource#throwNoResultException()
	 */
	@Test
	public void testNoResultException() throws IOException {
		final HttpDelete httpdelete = new HttpDelete(BASE_URI + RESOURCE + "/noResultException");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
			Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
			final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			final Map<?, ?> result = new ObjectMapperTrim().readValue(content, HashMap.class);
			Assert.assertEquals("entity", result.get("code"));
			Assert.assertEquals("message", result.get("message"));
			Assert.assertNull(result.get("cause"));
		} finally {
			if (response != null) {
				response.getEntity().getContent().close();
			}
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
