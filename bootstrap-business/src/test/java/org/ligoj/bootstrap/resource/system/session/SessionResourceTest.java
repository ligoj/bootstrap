package org.ligoj.bootstrap.resource.system.session;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.transaction.Transactional;

import org.ligoj.bootstrap.AbstractJpaTest;
import org.ligoj.bootstrap.core.SpringUtils;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.ligoj.bootstrap.resource.system.cache.CacheResource;

/**
 * {@link SessionResource} test class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/jpa-context-test.xml", "classpath:/META-INF/spring/security-context-test.xml",
		"classpath:/META-INF/spring/business-context-test.xml" })
@Rollback
@Transactional
public class SessionResourceTest extends AbstractJpaTest {

	@Autowired
	private CacheResource cacheResource;

	/**
	 * Remote REST server.
	 */
	@Autowired
	private SessionResource resource;

	@SuppressWarnings("unchecked")
	@Before
	public void mockApplicationContext() {
		final ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
		SpringUtils.setSharedApplicationContext(applicationContext);
		Mockito.when(applicationContext.getBean(SessionSettings.class)).thenReturn(new SessionSettings());
		Mockito.when(applicationContext.getBean(ArgumentMatchers.any(Class.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) {
				final Class<?> requiredType = (Class<Object>) invocation.getArguments()[0];
				if (requiredType == SessionSettings.class) {
					return new SessionSettings();
				}
				return SessionResourceTest.super.applicationContext.getBean(requiredType);
			}
		});
	}

	@After
	public void unmockApplicationContext() {
		SpringUtils.setSharedApplicationContext(super.applicationContext);
	}

	/**
	 * Username is provided, plenty of authorities
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testReadUserSettings() {
		final SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);
		final SystemRole role = new SystemRole();
		role.setName(DEFAULT_ROLE);
		em.persist(role);

		final SystemUserSetting userSetting = new SystemUserSetting();
		userSetting.setLogin(DEFAULT_USER);
		userSetting.setName("k");
		userSetting.setValue("v");
		em.persist(userSetting);

		final Collection<GrantedAuthority> authorities = new ArrayList<>();
		Mockito.when((Collection) SecurityContextHolder.getContext().getAuthentication().getAuthorities()).thenReturn(authorities);
		authorities.add(role);
		final SystemRoleAssignment assignment = new SystemRoleAssignment();
		assignment.setRole(role);
		assignment.setUser(user);
		em.persist(assignment);

		addSystemAuthorization(HttpMethod.GET, role, "^myurl1", AuthorizationType.BUSINESS);
		addSystemAuthorization(null, role, "^myurl2", AuthorizationType.UI);

		// Invalidate cache of previous test
		cacheResource.invalidate("authorizations");
		em.flush();
		em.clear();

		// Get result
		final SessionSettings settings = resource.details();

		// Check the application settings (session scope)
		Assert.assertNotNull(settings);
		Assert.assertNotNull(settings.getRoles());
		Assert.assertEquals(1, settings.getRoles().size());
		Assert.assertEquals(DEFAULT_ROLE, settings.getRoles().get(0));
		Assert.assertNotNull(settings.getAuthorizations());
		Assert.assertEquals(1, settings.getAuthorizations().size());
		Assert.assertEquals("^myurl2", settings.getAuthorizations().iterator().next());
		Assert.assertNotNull(settings.getBusinessAuthorizations());
		Assert.assertEquals(1, settings.getBusinessAuthorizations().size());
		Assert.assertEquals("^myurl1", settings.getBusinessAuthorizations().get(0).getPattern());
		Assert.assertEquals("GET", settings.getBusinessAuthorizations().get(0).getMethod().toString());
		Assert.assertNotNull(settings.getUserSettings());
		Assert.assertFalse(settings.getUserSettings().isEmpty());
		Assert.assertEquals("v", settings.getUserSettings().get("k"));
	}

	/**
	 * Username is provided, with roles giving no authorizations
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void detailsSoloRole() {
		final SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);
		final SystemRole soloRole = new SystemRole();
		soloRole.setName("SOLO");
		em.persist(soloRole);

		final Collection<GrantedAuthority> authorities = new ArrayList<>();
		Mockito.when((Collection) SecurityContextHolder.getContext().getAuthentication().getAuthorities()).thenReturn(authorities);
		authorities.add(soloRole);
		final SystemRoleAssignment assignmentSolo = new SystemRoleAssignment();
		assignmentSolo.setRole(soloRole);
		assignmentSolo.setUser(user);
		em.persist(assignmentSolo);

		// Invalidate cache of previous test
		cacheResource.invalidate("authorizations");

		// Get result
		final SessionSettings settings = resource.details();

		// Check the application settings (session scope)
		Assert.assertNotNull(settings);
		Assert.assertNotNull(settings.getRoles());
		Assert.assertEquals(1, settings.getRoles().size());
		Assert.assertEquals("SOLO", settings.getRoles().get(0));
		Assert.assertNotNull(settings.getAuthorizations());
		Assert.assertEquals(0, settings.getAuthorizations().size());
		Assert.assertNotNull(settings.getBusinessAuthorizations());
		Assert.assertEquals(0, settings.getBusinessAuthorizations().size());
	}

	/**
	 * Username is provided, without authority
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void detailsNoRole() {
		final SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);

		final Collection<GrantedAuthority> authorities = new ArrayList<>();
		Mockito.when((Collection) SecurityContextHolder.getContext().getAuthentication().getAuthorities()).thenReturn(authorities);

		// Invalidate cache of previous test
		cacheResource.invalidate("authorizations");

		// Get result
		final SessionSettings settings = resource.details();

		// Check the application settings (session scope)
		Assert.assertNotNull(settings);
		Assert.assertNotNull(settings.getRoles());
		Assert.assertEquals(0, settings.getRoles().size());
		Assert.assertNotNull(settings.getAuthorizations());
		Assert.assertEquals(0, settings.getAuthorizations().size());
		Assert.assertNotNull(settings.getBusinessAuthorizations());
		Assert.assertEquals(0, settings.getBusinessAuthorizations().size());
	}

	/**
	 * Username is provided, plenty of authorities + SystemRole#DEFAULT_ROLE
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void detailsNoRoleButDefaultRole() {
		final SystemUser user = new SystemUser();
		user.setLogin(DEFAULT_USER);
		em.persist(user);
		final SystemRole role = new SystemRole();
		role.setName("SOLO");
		em.persist(role);

		final Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(SystemRole.DEFAULT_ROLE));
		Mockito.when((Collection) SecurityContextHolder.getContext().getAuthentication().getAuthorities()).thenReturn(authorities);
		authorities.add(role);
		final SystemRoleAssignment assignment = new SystemRoleAssignment();
		assignment.setRole(role);
		assignment.setUser(user);
		em.persist(assignment);

		addSystemAuthorization(null, role, "^myurl2", AuthorizationType.UI);

		// Invalidate cache of previous test
		cacheResource.invalidate("authorizations");

		// Get result
		final SessionSettings settings = resource.details();

		// Check the application settings (session scope)
		Assert.assertNotNull(settings);
		Assert.assertNotNull(settings.getRoles());
		Assert.assertEquals(2, settings.getRoles().size());
		Assert.assertNotNull(settings.getAuthorizations());
		Assert.assertEquals(1, settings.getAuthorizations().size());
		Assert.assertEquals("^myurl2", settings.getAuthorizations().iterator().next());
	}

	private void addSystemAuthorization(final HttpMethod method, SystemRole role, final String pattern, final AuthorizationType type) {
		final SystemAuthorization authorization = new SystemAuthorization();
		authorization.setRole(role);
		authorization.setMethod(method);
		authorization.setType(type);
		authorization.setPattern(pattern);
		em.persist(authorization);
	}
}
