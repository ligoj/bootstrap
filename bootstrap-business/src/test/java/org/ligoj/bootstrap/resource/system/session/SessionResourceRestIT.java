/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.session;

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
import org.ligoj.bootstrap.AbstractRestTest;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;

import java.io.IOException;

/**
 * Spring Security configuration and {@link SessionResource} access from REST
 * interface.
 */
class SessionResourceRestIT extends AbstractRestTest {

	/**
	 * URI
	 */
	private static final String RESOURCE = "/test/crud";

	/**
	 * URI
	 */
	private static final String SESSION_RESOURCE = "session";

	/**
	 * Remote REST server.
	 */
	private static Server server;

	/**
	 * server creation.
	 */
	@BeforeAll
	static void startServer() {
		server = new SessionResourceRestIT().startRestServer(null);
	}

	/**
	 * No username provided gives 403
	 */
	@Test
	void testProtectedResourceAnonymousMeans403() throws IOException {
		final var message = new HttpPost(BASE_URI + RESOURCE);
		message.setEntity(new StringEntity("{\"id\":0}", ContentType.APPLICATION_JSON));
		httpclient.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, response.getCode());
			return null;
		});
	}

	/**
	 * Username is provided but without authority and not in white list gives
	 * 403
	 */
	@Test
	void testProtectedResourceNotAuthorizedMeans403() throws IOException {
		final var message = new HttpPost(BASE_URI + "/" + SESSION_RESOURCE);
		message.addHeader("sm_universalid", "session");
		message.setEntity(new StringEntity("{\"id\":0}", ContentType.APPLICATION_JSON));
		httpclient.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, response.getCode());
			return null;
		});
	}

	/**
	 * Username is provided, without authority but in white list gives 200
	 */
	@Test
	void testProtectedWithAuthorization() throws IOException {
		final var message = new HttpGet(BASE_URI + "/" + SESSION_RESOURCE);

		// Add TARS headers
		message.addHeader("sm_universalid", "session");
		httpclient.execute(message, response -> {
			Assertions.assertEquals(HttpStatus.SC_OK, response.getCode());

			final var settings = new ObjectMapperTrim().readValue(response.getEntity().getContent(), SessionSettings.class);

			// Check the application settings (session scope)
			Assertions.assertNotNull(settings);
			Assertions.assertNotNull(settings.getRoles());
			Assertions.assertTrue(settings.getRoles().contains("USER"));
			Assertions.assertNotNull(settings.getApiAuthorizations());
			Assertions.assertNotNull(settings.getUiAuthorizations());
			Assertions.assertEquals("session", settings.getUserName());

			// Check the application settings (singleton)
			Assertions.assertNotNull(settings.getApplicationSettings());
			Assertions.assertNotNull(settings.getApplicationSettings().getBuildNumber());
			Assertions.assertNotNull(settings.getApplicationSettings().getBuildTimestamp());
			Assertions.assertNotNull(settings.getApplicationSettings().getBuildVersion());
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
