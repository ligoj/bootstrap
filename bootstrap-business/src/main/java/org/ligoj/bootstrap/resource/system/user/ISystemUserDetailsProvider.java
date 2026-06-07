/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.user;

import java.util.Collection;

import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * A provider of extended {@link SystemUser} details — attributes living outside this layer (e.g. an IAM cache entity
 * sharing the same identifier) such as first name, last name and mails. Implementors are discovered from the Spring
 * context (same pattern as {@link org.ligoj.bootstrap.resource.system.session.ISessionSettingsProvider}): the first
 * available provider replaces the local lookup of {@link UserResource#findAllWithRoles} ({@link #findAll}), and all
 * providers enrich the resulting page ({@link #decorate(Collection)}).
 */
public interface ISystemUserDetailsProvider {

	/**
	 * Perform the paginated system user lookup, matching the given criteria against the login AND the extended
	 * details this provider knows (first name, last name, mails, ...). The pagination is applied by the
	 * implementation — never return an unbounded result.
	 *
	 * @param criteria The lookup criteria. Never <code>null</code>, empty matches all users.
	 * @param page     The pagination/sort information.
	 * @return The matching users page.
	 */
	Page<SystemUser> findAll(String criteria, Pageable page);

	/**
	 * Fill the extended details (first name, last name, mails, ...) of the given users when known.
	 *
	 * @param users The users page to decorate, identified by their login.
	 */
	default void decorate(final Collection<SystemUserVo> users) {
		// No decoration by default
	}
}
