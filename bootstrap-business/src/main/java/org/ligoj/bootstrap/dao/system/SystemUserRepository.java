/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

/**
 * User repository.
 */
public interface SystemUserRepository extends RestRepository<SystemUser, String> {

	/**
	 * Lookup criteria: empty criteria matches all, otherwise the login is matched. The extended details (mails,
	 * names, ...) are only searchable through a
	 * {@link org.ligoj.bootstrap.resource.system.user.ISystemUserDetailsProvider} implementation replacing this
	 * fallback lookup.
	 */
	String LOOKUP_CRITERIA = " WHERE :criteria = ''"
			+ " OR UPPER(u.login) LIKE UPPER(CONCAT(CONCAT('%',:criteria),'%'))";

	/**
	 * Return the users with their roles fetched, matching the given criteria against the login.
	 *
	 * @param criteria The lookup criteria, or an empty string to match all.
	 * @param page     The pagination/sort information.
	 * @return The matching users with fetched roles.
	 */
	@Query(value = "SELECT u FROM SystemUser u LEFT JOIN FETCH u.roles ra LEFT JOIN FETCH ra.role" + LOOKUP_CRITERIA,
			countQuery = "SELECT COUNT(u) FROM SystemUser u" + LOOKUP_CRITERIA)
	Page<SystemUser> findAllDetailed(String criteria, Pageable page);

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
