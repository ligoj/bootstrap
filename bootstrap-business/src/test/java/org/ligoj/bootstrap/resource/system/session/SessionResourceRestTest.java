package org.ligoj.bootstrap.resource.system.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.ligoj.bootstrap.AbstractRestTest;
import org.ligoj.bootstrap.core.SpringUtils;
import org.ligoj.bootstrap.core.json.ObjectMapper;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.resource.system.security.AuthorizationEditionVo;
import org.ligoj.bootstrap.resource.system.security.RoleResource;
import org.ligoj.bootstrap.resource.system.security.SystemRoleVo;
import org.ligoj.bootstrap.resource.system.user.SystemUserEditionVo;
import org.ligoj.bootstrap.resource.system.user.UserResource;

/**
 * Spring Security configuration and {@link SessionResource} access from REST interface.
 */
@FixMethodOrder(MethodSorters.JVM)
public class SessionResourceRestTest extends AbstractRestTest {

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

	private static boolean setUpIsDone = false;

	/**
	 * server creation.
	 */
	@BeforeClass
	public static void startServer() {
		server = new SessionResourceRestTest().startRestServer("./src/test/resources/WEB-INF/web-test-security.xml");
	}

	/**
	 * Create a role and one authorization for user DEFAULT_USER. Only run once.
	 */
	@Before
	public void prepareSecurityContext() {
		if (setUpIsDone) {
			return;
		}

		final RoleResource resource = SpringUtils.getBean(RoleResource.class);
		final UserResource userResource = SpringUtils.getBean(UserResource.class);

		// Create the authorization
		final AuthorizationEditionVo authorization = new AuthorizationEditionVo();
		authorization.setPattern(SESSION_RESOURCE);
		authorization.setType(AuthorizationType.BUSINESS);
		final List<AuthorizationEditionVo> authorizations = new ArrayList<>();
		authorizations.add(authorization);

		// Create the role
		final SystemRoleVo role = new SystemRoleVo();
		role.setName("test");
		role.setAuthorizations(authorizations);
		final int roleId = resource.create(role);

		final SystemUserEditionVo user = new SystemUserEditionVo();
		user.setLogin(DEFAULT_USER);
		final List<Integer> roles = new ArrayList<>();
		roles.add(roleId);
		user.setRoles(roles);
		userResource.create(user);

		setUpIsDone = true;
	}

	/**
	 * No username provided gives 403
	 */
	@Test
	public void testProtectedResourceAnonymousMeans403() throws IOException {
		final HttpPost message = new HttpPost(BASE_URI + RESOURCE);
		message.setEntity(new StringEntity("{\"id\":0}", ContentType.APPLICATION_JSON));
		final HttpResponse response = httpclient.execute(message);
		try {
			Assert.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		} finally {
			IOUtils.closeQuietly(response.getEntity().getContent());
		}
	}

	/**
	 * Username is provided but without authority and not in white list gives 403
	 */
	@Test
	public void testProtectedResourceNotAuthorizedMeans403() throws IOException {
		final HttpPost message = new HttpPost(BASE_URI + "/" + SESSION_RESOURCE);
		message.addHeader("sm_universalid", "any");
		message.setEntity(new StringEntity("{\"id\":0}", ContentType.APPLICATION_JSON));
		final HttpResponse response = httpclient.execute(message);
		try {
			Assert.assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
		} finally {
			IOUtils.closeQuietly(response.getEntity().getContent());
		}
	}

	/**
	 * Username is provided, without authority but in white list gives 200
	 */
	@Test
	public void testProtectedWithAuthorization() throws IOException {
		final HttpGet message = new HttpGet(BASE_URI + "/" + SESSION_RESOURCE);

		// Add TARS headers
		message.addHeader("sm_universalid", DEFAULT_USER);
		final HttpResponse response = httpclient.execute(message);

		try {
			Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

			final SessionSettings settings = new ObjectMapper().readValue(response.getEntity().getContent(), SessionSettings.class);

			// Check the application settings (session scope)
			Assert.assertNotNull(settings);
			Assert.assertNotNull(settings.getRoles());
			Assert.assertEquals(2, settings.getRoles().size());
			Assert.assertTrue(settings.getRoles().contains("USER"));
			Assert.assertTrue(settings.getRoles().contains("test"));
			Assert.assertNotNull(settings.getAuthorizations());
			Assert.assertNotNull(settings.getBusinessAuthorizations());
			Assert.assertEquals(DEFAULT_USER, settings.getUserName());

			// Check the application settings (singleton)
			Assert.assertNotNull(settings.getApplicationSettings());
			Assert.assertNotNull(settings.getApplicationSettings().getBuildNumber());
			Assert.assertNotNull(settings.getApplicationSettings().getBuildTimestamp());
			Assert.assertNotNull(settings.getApplicationSettings().getBuildVersion());
		} finally {
			IOUtils.closeQuietly(response.getEntity().getContent());
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
