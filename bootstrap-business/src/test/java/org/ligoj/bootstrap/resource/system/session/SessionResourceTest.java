package org.ligoj.bootstrap.resource.system.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.model.system.SystemUserSetting;
import org.ligoj.bootstrap.resource.system.cache.CacheResource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * {@link SessionResource} test class.
 */
@ExtendWith(SpringExtension.class)
public class SessionResourceTest extends AbstractBootTest {

	@Autowired
	private CacheResource cacheResource;

	private SessionResource resource;

	@BeforeEach
	public void mockApplicationContext() {
		resource = new SessionResource();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
		final SessionSettings settings = new SessionSettings();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(settings);
		ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
		resource.applicationContext = applicationContext;
		Mockito.when(applicationContext.getBean(SessionSettings.class)).thenReturn(settings);
		final ISessionSettingsProvider provider = Mockito.mock(ISessionSettingsProvider.class);
		Mockito.when(resource.applicationContext.getBeansOfType(ISessionSettingsProvider.class))
				.thenReturn(Collections.singletonMap("provider", provider));
	}

	@Test
	public void getUserSettings() {
		final SessionSettings settings = resource.applicationContext.getBean(SessionSettings.class);
		Assertions.assertEquals(DEFAULT_USER, settings.getUserName());
		final ApplicationSettings applicationSettings = settings.getApplicationSettings();
		Assertions.assertNotNull(applicationSettings.getBuildNumber());
		Assertions.assertNotNull(applicationSettings.getBuildTimestamp());
		Assertions.assertNotNull(applicationSettings.getBuildVersion());
	}

	/**
	 * Username is provided, plenty of authorities
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void detailsUserSettings() {
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

		addSystemAuthorization(HttpMethod.GET, role, "^myurl1", AuthorizationType.API);
		addSystemAuthorization(null, role, "^myurl2", AuthorizationType.UI);

		// Invalidate cache of previous test
		cacheResource.invalidate("authorizations");
		em.flush();
		em.clear();

		// Get result
		final SessionSettings settings = resource.details();

		// Check the application settings (session scope)
		Assertions.assertNotNull(settings);
		Assertions.assertNotNull(settings.getRoles());
		Assertions.assertEquals(1, settings.getRoles().size());
		Assertions.assertEquals(DEFAULT_ROLE, settings.getRoles().get(0));
		Assertions.assertNotNull(settings.getAuthorizations());
		Assertions.assertEquals(1, settings.getAuthorizations().size());
		Assertions.assertEquals("^myurl2", settings.getAuthorizations().iterator().next());
		Assertions.assertNotNull(settings.getBusinessAuthorizations());
		Assertions.assertEquals(1, settings.getBusinessAuthorizations().size());
		Assertions.assertEquals("^myurl1", settings.getBusinessAuthorizations().get(0).getPattern());
		Assertions.assertEquals("GET", settings.getBusinessAuthorizations().get(0).getMethod().toString());
		Assertions.assertNotNull(settings.getUserSettings());
		Assertions.assertFalse(settings.getUserSettings().isEmpty());
		Assertions.assertEquals("v", settings.getUserSettings().get("k"));
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
		Assertions.assertNotNull(settings);
		Assertions.assertNotNull(settings.getRoles());
		Assertions.assertEquals(1, settings.getRoles().size());
		Assertions.assertEquals("SOLO", settings.getRoles().get(0));
		Assertions.assertNotNull(settings.getAuthorizations());
		Assertions.assertEquals(0, settings.getAuthorizations().size());
		Assertions.assertNotNull(settings.getBusinessAuthorizations());
		Assertions.assertEquals(0, settings.getBusinessAuthorizations().size());
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
		Assertions.assertNotNull(settings);
		Assertions.assertNotNull(settings.getRoles());
		Assertions.assertEquals(0, settings.getRoles().size());
		Assertions.assertNotNull(settings.getAuthorizations());
		Assertions.assertEquals(0, settings.getAuthorizations().size());
		Assertions.assertNotNull(settings.getBusinessAuthorizations());
		Assertions.assertEquals(0, settings.getBusinessAuthorizations().size());
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
		Assertions.assertNotNull(settings);
		Assertions.assertNotNull(settings.getRoles());
		Assertions.assertEquals(2, settings.getRoles().size());
		Assertions.assertNotNull(settings.getAuthorizations());
		Assertions.assertEquals(1, settings.getAuthorizations().size());
		Assertions.assertEquals("^myurl2", settings.getAuthorizations().iterator().next());
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
