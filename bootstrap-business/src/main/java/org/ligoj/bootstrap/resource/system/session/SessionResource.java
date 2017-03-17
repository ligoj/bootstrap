package org.ligoj.bootstrap.resource.system.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ligoj.bootstrap.dao.system.AuthorizationRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.ligoj.bootstrap.model.system.SystemAuthorization.AuthorizationType;
import org.ligoj.bootstrap.resource.system.security.AuthorizationResource;
import org.ligoj.bootstrap.resource.system.user.UserSettingResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Session resource.
 */
@Path("/session")
@Service
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

	@Autowired
	protected AuthorizationRepository repository;

	@Autowired
	protected UserSettingResource userSettingResource;

	@Autowired
	protected ApplicationContext applicationContext;

	/**
	 * Memory safe empty authorization.
	 */
	private static final List<Map<HttpMethod, List<Pattern>>> EMPTY_ROLES = new ArrayList<>();

	@Autowired
	private AuthorizationResource authorizationResource;

	/**
	 * Current session settings.
	 * 
	 * @return The current session settings with authorizations and roles.
	 */
	@GET
	@Transactional
	public SessionSettings details() {
		// Get the session scoped bean
		final SessionSettings settings = applicationContext.getBean(SessionSettings.class);

		// Add user settings
		settings.setUserSettings(userSettingResource.findAll());

		// Add authorizations
		addAuthorizations(settings);

		// Ask providers to complete the session details
		applicationContext.getBeansOfType(ISessionSettingsProvider.class).values().forEach(p -> p.decorate(settings));

		return settings;
	}

	/**
	 * Add roles and authorizations.
	 */
	private void addAuthorizations(final SessionSettings settings) {
		final List<String> rolesAsString = getRolesAsString();
		settings.setRoles(rolesAsString);

		// Add authorizations
		final Map<AuthorizationType, Map<String, Map<HttpMethod, List<Pattern>>>> cache = authorizationResource.getAuthorizations();
		settings.setAuthorizations(toPatterns(filterRoles(cache.get(AuthorizationType.UI), rolesAsString)));
		settings.setBusinessAuthorizations(getBusinessAuthorizations(filterRoles(cache.get(AuthorizationType.BUSINESS), rolesAsString)));
	}

	/**
	 * Return only authorization for the granted authorities.
	 */
	private List<Map<HttpMethod, List<Pattern>>> filterRoles(final Map<String, Map<HttpMethod, List<Pattern>>> authorizations,
			final List<String> rolesAsString) {
		if (authorizations == null) {
			// No authorization -> no roles
			return EMPTY_ROLES;
		}
		return rolesAsString.stream().map(authorizations::get).filter(Objects::nonNull).collect(Collectors.toList());
	}

	/**
	 * Return all flattered patterns.
	 */
	private Set<String> toPatterns(final List<Map<HttpMethod, List<Pattern>>> authorizations) {
		return authorizations.stream().map(Map::values).flatMap(Collection::stream).flatMap(Collection::stream).map(Pattern::pattern)
				.collect(Collectors.toSet());
	}

	/**
	 * Build and return the list of roles.
	 */
	private List<String> getRolesAsString() {
		final Collection<? extends GrantedAuthority> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		return roles.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
	}

	/**
	 * Build and return the list of business authorizations.
	 */
	private List<SystemAuthorization> getBusinessAuthorizations(final List<Map<HttpMethod, List<Pattern>>> authorizations) {
		return authorizations.stream().map(Map::entrySet).flatMap(Collection::stream).flatMap(entry -> entry.getValue().stream().map(pattern -> {
			final SystemAuthorization businessAuthorization = new SystemAuthorization();
			businessAuthorization.setMethod(entry.getKey());
			businessAuthorization.setPattern(pattern.pattern());
			return businessAuthorization;
		})).collect(Collectors.toList());
	}
}
