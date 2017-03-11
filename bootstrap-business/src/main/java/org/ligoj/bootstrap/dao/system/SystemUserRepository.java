package org.ligoj.bootstrap.dao.system;

import org.springframework.data.jpa.repository.Query;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemUser;

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
	@Query(value = "SELECT user, r FROM SystemUser user LEFT JOIN user.roles ra LEFT JOIN ra.role r WHERE user.login = ?1")
	Object[][] findByLoginFetchRoles(String login);
}
