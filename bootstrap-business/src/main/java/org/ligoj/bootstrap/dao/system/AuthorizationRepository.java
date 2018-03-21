/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import java.util.List;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.springframework.data.jpa.repository.Query;

/**
 * Authorization repository.
 */
public interface AuthorizationRepository extends RestRepository<SystemAuthorization, Integer> {

	/**
	 * Return all authorizations from the assigned roles to given user whatever the context, date or applied resource.
	 * 
	 * @param login
	 *            the user login.
	 * @param type
	 *            authorization type filter.
	 * @return all authorizations from the assigned roles to given user whatever the context, date or applied resource.
	 */
	@Query("FROM SystemAuthorization sa LEFT JOIN FETCH sa.role role "
			+ "WHERE sa.type = ?2 AND role IN (SELECT DISTINCT sra.role FROM SystemRoleAssignment sra WHERE sra.user.login = ?1)")
	List<SystemAuthorization> findAllByLogin(String login, SystemAuthorization.AuthorizationType type);

	/**
	 * Return all authorizations from the assigned roles to given user whatever the context, date or applied resource.
	 * 
	 * @param login
	 *            the user login.
	 * @param type
	 *            authorization type filter.
	 * @return all authorizations from the assigned roles to given user whatever the context, date or applied resource.
	 */
	@Query("SELECT DISTINCT sa.pattern FROM SystemAuthorization sa LEFT JOIN sa.role role "
			+ "WHERE sa.type = ?2 AND role IN (SELECT DISTINCT sra.role FROM SystemRoleAssignment sra WHERE sra.user.login = ?1)")
	List<String> findAllPatternsByLogin(String login, SystemAuthorization.AuthorizationType type);

	/**
	 * Return all authorizations by type.
	 * 
	 * @param type
	 *            authorization type filter.
	 * @return all authorizations of given type.
	 */
	@Query("FROM SystemAuthorization sa LEFT JOIN FETCH sa.role role WHERE sa.type = ?1")
	List<SystemAuthorization> findAllByType(SystemAuthorization.AuthorizationType type);

}
