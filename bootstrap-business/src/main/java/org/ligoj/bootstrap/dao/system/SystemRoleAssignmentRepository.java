/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import java.util.List;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemRole;
import org.ligoj.bootstrap.model.system.SystemRoleAssignment;
import org.springframework.data.jpa.repository.Query;

/**
 * SystemRoleAssignment repository.
 */
public interface SystemRoleAssignmentRepository extends RestRepository<SystemRoleAssignment, Integer> {

	/**
	 * Return all roles whatever the context, date or applied resource.
	 * 
	 * @param login
	 *            the user login.
	 * @return all roles whatever the context, date or applied resource.
	 */
	@Query("SELECT DISTINCT sra.role FROM SystemRoleAssignment sra WHERE sra.user.login = ?1")
	List<SystemRole> findAllRolesByLogin(String login);

}
