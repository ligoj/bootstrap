package org.ligoj.bootstrap.core.security;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.resource.mapper.AccessDeniedExceptionMapper;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.resource.system.cache.CacheResource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class of {@link AuthorizingFilter}
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthorizingFilterTest extends AbstractBootTest {

	@Autowired
	private SystemRoleRepository systemRoleRepository;

	private AccessDeniedExceptionMapper accessDeniedExceptionMapper;

	@Autowired
	private AuthorizingFilter authorizingFilter;

	@Autowired
	private CacheResource cacheResource;

	@Before
	public void setup() {
		accessDeniedExceptionMapper = Mockito.mock(AccessDeniedExceptionMapper.class);
		final Response responseJson = Response.status(Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).entity("json").build();
		Mockito.when(accessDeniedExceptionMapper.toResponse(ArgumentMatchers.any(AccessDeniedException.class))).thenReturn(responseJson);

		final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((GenericApplicationContext) applicationContext).getBeanFactory();
		beanFactory.registerSingleton("accessDeniedExceptionMapper", accessDeniedExceptionMapper);

	}

	@After
	public void teardown() {
		final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ((GenericApplicationContext) applicationContext).getBeanFactory();
		beanFactory.destroySingleton("accessDeniedExceptionMapper");
	}

	/**
	 * No authority
	 */
	@Test
	public void doFilterNoAuthority() throws Exception {
		final FilterChain chain = Mockito.mock(FilterChain.class);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final ServletContext servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(outputStream, Mockito.atLeastOnce()).write(ArgumentMatchers.any(byte[].class));
		Mockito.verify(chain, Mockito.never()).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Plenty defined authority
	 */
	@Test
	public void doFilterPlentyAuthority() throws Exception {

		for (final HttpMethod method : HttpMethod.values()) {
			addSystemAuthorization(method, "role1", "^myurl");
			addSystemAuthorization(method, "role2", "^myurl");
		}
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final FilterChain chain = Mockito.mock(FilterChain.class);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final ServletContext servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		authorizingFilter.doFilter(request, response, chain);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(outputStream, Mockito.atLeastOnce()).write(ArgumentMatchers.any(byte[].class));
		Mockito.verify(chain, Mockito.never()).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Anonymous user / role
	 */
	@Test
	public void doFilterAnonymous() throws Exception {
		cacheResource.invalidate("authorizations");
		attachRole("ROLE_ANONYMOUS");
		final FilterChain chain = Mockito.mock(FilterChain.class);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.atLeastOnce()).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Plenty attached authority
	 */
	@Test
	public void doFilterAttachedAuthority() throws Exception {
		cacheResource.invalidate("authorizations");
		attachRole(DEFAULT_ROLE, "other");
		final FilterChain chain = Mockito.mock(FilterChain.class);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final ServletContext servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		authorizingFilter.doFilter(request, response, chain);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(outputStream, Mockito.atLeastOnce()).write(ArgumentMatchers.any(byte[].class));
		Mockito.verify(chain, Mockito.never()).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Plenty attached authority
	 */
	@Test
	public void doFilterAttachedAuthority2() throws Exception {
		attachRole(DEFAULT_ROLE, "role1", "role2", "role3");
		for (final HttpMethod method : HttpMethod.values()) {
			addSystemAuthorization(method, "role1", "^myurl");
			addSystemAuthorization(method, "role2", "^myurl");
			addSystemAuthorization(null, "role1", "^youurl");
			addSystemAuthorization(null, "role2", "^yoururl");
		}
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final FilterChain chain = Mockito.mock(FilterChain.class);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final ServletContext servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.when(request.getMethod()).thenReturn("HEAD");
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(outputStream, Mockito.atLeastOnce()).write(ArgumentMatchers.any(byte[].class));
		Mockito.verify(chain, Mockito.never()).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Plenty attached authority
	 */
	@Test
	public void doFilterAttachedAuthority3() throws Exception {
		attachRole(DEFAULT_ROLE, "role2");
		addSystemAuthorization(HttpMethod.GET, "role2", "^match$");
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final FilterChain chain = Mockito.mock(FilterChain.class);
		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		final ServletContext servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/match");
		Mockito.when(request.getQueryString()).thenReturn("query");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
		final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.when(request.getMethod()).thenReturn("HEAD");
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.atLeastOnce()).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	private void addSystemAuthorization(final HttpMethod method, final String roleName, final String pattern) {
		SystemRole role = systemRoleRepository.findByName(roleName);
		if (role == null) {
			role = new SystemRole();
			role.setName(roleName);
			em.persist(role);
		}
		final SystemAuthorization authorization = new SystemAuthorization();
		authorization.setRole(role);
		authorization.setMethod(method);
		authorization.setType(AuthorizationType.BUSINESS);
		authorization.setPattern(pattern);
		em.persist(authorization);
	}

	@SuppressWarnings("rawtypes")
	private void attachRole(final String... roleNames) {

		final Collection<GrantedAuthority> authorities = new ArrayList<>();
		Mockito.when((Collection) SecurityContextHolder.getContext().getAuthentication().getAuthorities()).thenReturn(authorities);
		for (final String roleName : roleNames) {
			authorities.add(new SimpleGrantedAuthority(roleName));
		}
	}
}
