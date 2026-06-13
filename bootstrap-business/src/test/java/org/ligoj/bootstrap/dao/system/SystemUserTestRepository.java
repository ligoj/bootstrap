/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.data.jpa.repository.Query;

/**
 * Test repository exercising the {@link SystemUser#IS_ADMIN} SpEL fragment within a real Spring Data query, the way the
 * downstream visibility queries (delegates, projects, nodes, containers, ...) use it.
 */
public interface SystemUserTestRepository extends RestRepository<SystemUser, String> {

	/**
	 * Return <code>true</code> when the given user exists <strong>and</strong> the current principal is an
	 * administrator. The administrator part is resolved by the {@link SystemUser#IS_ADMIN} SpEL bind parameter.
	 *
	 * @param user The user login to match.
	 * @return <code>true</code> when the user matches and the current principal is an administrator.
	 */
	@Query("SELECT COUNT(u) > 0 FROM SystemUser u WHERE u.login = :user AND " + SystemUser.IS_ADMIN)
	boolean isAdmin(String user);
}
