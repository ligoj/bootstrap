/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.session;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * ConnectedUser settings including.
 */
@Component
@Scope("session")
@Getter
public class SessionSettings {

	/**
	 * Session identifier of current user.
	 */
	@Autowired
	private ApplicationSettings applicationSettings;

	/**
	 * User settings. May be empty.
	 */
	@Setter
	private Map<String, Object> userSettings;

	/**
	 * UI Authorizations.
	 */
	@Setter
	private Set<String> authorizations;

	/**
	 * Business authorizations.
	 */
	@Setter
	private List<SystemAuthorization> businessAuthorizations;

	/**
	 * Roles.
	 */
	@Setter
	private List<String> roles;

	/**
	 * User details.
	 */
	@Value("#{T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()}")
	private String userName;

}
