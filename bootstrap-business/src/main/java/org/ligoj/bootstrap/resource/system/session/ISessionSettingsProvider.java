/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.session;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Contract to decorate the session settings with any data.
 */
@FunctionalInterface
public interface ISessionSettingsProvider {

	/**
	 * Decorate the session settings.
	 *
	 * @param settings The current session settings. Note it contains at least the user session settings.
	 */
	void decorate(SessionSettings settings);

	/**
	 * Return addition roles granted by plugins.
	 *
	 * @param username Current username.
	 * @return a non-null list of role names.
	 */
	default Collection<GrantedAuthority> getGrantedAuthorities(final String username) {
		return Collections.emptyList();
	}
}
