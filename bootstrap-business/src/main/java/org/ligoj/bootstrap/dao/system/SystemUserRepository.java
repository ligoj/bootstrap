/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.data.jpa.repository.Query;

/**
 * User repository.
 */
public interface SystemUserRepository extends RestRepository<SystemUser, String> {

	/**
	 * Return user and his/her roles.
	 *
	 * @param login
	 *            user login.
	 * @return {@link SystemUser} with roles.
	 */
	@Query("SELECT user, r FROM SystemUser user LEFT JOIN user.roles ra LEFT JOIN ra.role r WHERE user.login = ?1")
	Object[][] findByLoginFetchRoles(String login);

	/**
	 * Return <code>true</code> when given user is an administrator. Is
	 * considered administrators, user having all API authorization (.*)
	 * pattern.
	 *
	 * @param user
	 *            The username requesting the operation.
	 * @return <code>true</code> when the current user is an administrator.
	 */
	@Query("SELECT COUNT(s)>0 FROM SystemUser s WHERE " + SystemUser.IS_ADMIN)
	boolean isAdmin(String user);
}
