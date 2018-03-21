/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.session;

/**
 * Contract to decorate the session settings with any data.
 */
@FunctionalInterface
public interface ISessionSettingsProvider {

	/**
	 * Decorate the session settings.
	 * 
	 * @param settings
	 *            The current session settings. Note it contains at least the user session settings.
	 */
	void decorate(SessionSettings settings);
}
