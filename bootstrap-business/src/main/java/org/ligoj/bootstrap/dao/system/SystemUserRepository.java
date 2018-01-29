package org.ligoj.bootstrap.dao.system;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.data.jpa.repository.Query;

/**
 * User repository.
 */
public interface SystemUserRepository extends RestRepository<SystemUser, String> {

	/**
	 * Administrator role implicit criteria.
	 */
	String IS_ADMIN = "(EXISTS(SELECT 1 FROM SystemRoleAssignment ra INNER JOIN ra.role r WHERE ra.user.id = :user"
			+ "     AND EXISTS(SELECT 1 FROM SystemAuthorization a WHERE a.role = r AND a.pattern = '.*'"
			+ "          AND a.type = org.ligoj.bootstrap.model.system.SystemAuthorization$AuthorizationType.API)))";

	/**
	 * Return user and his/her roles.
	 * 
	 * @param login
	 *            user login.
	 * @return {@link SystemUser} with roles.
	 */
	@Query(value = "SELECT user, r FROM SystemUser user LEFT JOIN user.roles ra LEFT JOIN ra.role r WHERE user.login = ?1")
	Object[][] findByLoginFetchRoles(String login);

	/**
	 * Return <code>true</code> when given user is an administrator. Is
	 * considered administrators, user having all API authorization (.*)
	 * pattern.
	 * 
	 * @param user
	 *            The user name requesting the operation.
	 * @return <code>true</code> when the current user is an administrator.
	 */
	@Query("SELECT COUNT(s)>0 FROM SystemUser s WHERE " + IS_ADMIN)
	boolean isAdmin(String user);
}
