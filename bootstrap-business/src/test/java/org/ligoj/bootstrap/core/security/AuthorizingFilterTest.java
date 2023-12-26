/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.dao.system.SystemRoleRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.resource.system.cache.CacheResource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link AuthorizingFilter}
 */
@ExtendWith(SpringExtension.class)
class AuthorizingFilterTest extends AbstractBootTest {

	@Autowired
	private SystemRoleRepository systemRoleRepository;

	private AuthorizingFilter authorizingFilter;

	@Autowired
	private CacheResource cacheResource;

	@BeforeEach
	void prepare() {
		authorizingFilter = new AuthorizingFilter();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(authorizingFilter);
	}

	/**
	 * No authority
	 */
	@Test
	void doFilterNoAuthority() throws Exception {
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		final var response = Mockito.mock(HttpServletResponse.class);
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
	void doFilterPlentyAuthority() throws Exception {

		for (final var method : HttpMethod.values()) {
			addSystemAuthorization(method.name(), "role1", "^my_url");
			addSystemAuthorization(method.name(), "role2", "^my_url");
		}
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		final var response = Mockito.mock(HttpServletResponse.class);
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
	void doFilterAnonymous() throws Exception {
		cacheResource.invalidate("authorizations");
		attachRole("ROLE_ANONYMOUS");
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.atLeastOnce()).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Plenty attached authority
	 */
	@Test
	void doFilterAttachedAuthority() throws Exception {
		cacheResource.invalidate("authorizations");
		attachRole(DEFAULT_ROLE, "other");
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		final var response = Mockito.mock(HttpServletResponse.class);
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
	void doFilterAttachedAuthority2() throws Exception {
		attachRole(DEFAULT_ROLE, "role1", "role2", "role3");
		for (final var method : HttpMethod.values()) {
			addSystemAuthorization(method.name(), "role1", "^my_url");
			addSystemAuthorization(method.name(), "role2", "^my_url");
			addSystemAuthorization(null, "role1", "^you_url");
			addSystemAuthorization(null, "role2", "^your_url");
		}
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("context");
		Mockito.when(request.getRequestURI()).thenReturn("context/rest/any");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		final var response = Mockito.mock(HttpServletResponse.class);
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
	void doFilterAttachedAuthority3() throws Exception {
		attachRole(DEFAULT_ROLE, "role2");
		addSystemAuthorization(HttpMethod.GET.name(), "role2", "^rest/match$");
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("/context");
		Mockito.when(request.getRequestURI()).thenReturn("/context/rest/match");
		Mockito.when(request.getQueryString()).thenReturn("query");
		Mockito.when(request.getMethod()).thenReturn("GET");
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
		Mockito.when(request.getMethod()).thenReturn("HEAD");
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	/**
	 * Plenty attached authority
	 */
	@Test
	void doFilterViaUserNotAdmin() throws Exception {
		attachRole("SOME");
		addSystemAuthorization(HttpMethod.GET.name(), "SOME", ".*");
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("/context");
		Mockito.when(request.getRequestURI()).thenReturn("/context/rest/match");
		Mockito.when(request.getQueryString()).thenReturn("query");
		Mockito.when(request.getMethod()).thenReturn("GET");
		Mockito.when(request.getHeader("x-api-via-user")).thenReturn(DEFAULT_USER);
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.times(0)).doFilter(request, response);
		Mockito.verify(response, Mockito.times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
	}

	/**
	 * Plenty attached authority
	 */
	@Test
	void doFilterViaUser() throws Exception {
		attachRole("SOME");
		addSystemAuthorization(HttpMethod.GET.name(), "SOME", ".*");
		em.flush();
		em.clear();
		cacheResource.invalidate("authorizations");
		final var chain = Mockito.mock(FilterChain.class);
		final var request = Mockito.mock(HttpServletRequest.class);
		final var servletContext = Mockito.mock(ServletContext.class);
		Mockito.when(servletContext.getContextPath()).thenReturn("/context");
		Mockito.when(request.getRequestURI()).thenReturn("/context/rest/match");
		Mockito.when(request.getQueryString()).thenReturn("query");
		Mockito.when(request.getMethod()).thenReturn("GET");
		Mockito.when(request.getHeader("x-api-via-user")).thenReturn(DEFAULT_USER);
		final var outputStream = Mockito.mock(ServletOutputStream.class);
		final var response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(response.getOutputStream()).thenReturn(outputStream);
		authorizingFilter.setServletContext(servletContext);
		final var userDetailsService = Mockito.mock(RbacUserDetailsService.class);
		authorizingFilter.setUserDetailsService(userDetailsService);
		final var adminUser = new User(DEFAULT_USER, DEFAULT_USER, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
		Mockito.when(userDetailsService.loadUserByUsername(DEFAULT_USER)).thenReturn(adminUser);
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
		Mockito.when(request.getMethod()).thenReturn("HEAD");
		authorizingFilter.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
		Mockito.validateMockitoUsage();
	}

	private void addSystemAuthorization(final String method, final String roleName, final String pattern) {
		var role = systemRoleRepository.findByName(roleName);
		if (role == null) {
			role = new SystemRole();
			role.setName(roleName);
			em.persist(role);
		}
		final var authorization = new SystemAuthorization();
		authorization.setRole(role);
		authorization.setMethod(method);
		authorization.setType(AuthorizationType.API);
		authorization.setPattern(pattern);
		em.persist(authorization);
	}

	@SuppressWarnings("rawtypes")
	private void attachRole(final String... roleNames) {
		final var authorities = new ArrayList<>();
		Mockito.when((Collection) SecurityContextHolder.getContext().getAuthentication().getAuthorities()).thenReturn(authorities);
		Stream.of(roleNames).forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
	}
}
