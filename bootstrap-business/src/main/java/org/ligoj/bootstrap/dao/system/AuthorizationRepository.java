/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemAuthorization;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

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
	 * Return the names of the roles holding an administrative API authorization: an {@link SystemAuthorization}
	 * matching all URLs ({@code pattern = '.*'}), for all methods ({@code method IS NULL}) and of type
	 * {@link SystemAuthorization.AuthorizationType#API}. A principal granted one of these roles is an administrator.
	 *
	 * @return The administrative role names. May be empty, never <code>null</code>.
	 */
	@Query("SELECT sa.role.name FROM SystemAuthorization sa WHERE sa.pattern = '.*' AND sa.method IS NULL"
			+ " AND sa.type = org.ligoj.bootstrap.model.system.SystemAuthorization$AuthorizationType.API")
	Set<String> findAdminApiRoles();

}
