/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import java.util.List;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemApiToken;
import org.springframework.data.jpa.repository.Query;

/**
 * API token repository.
 */
public interface SystemApiTokenRepository extends RestRepository<SystemApiToken, Integer> {

	/**
	 * Return the name of the token matching the user.
	 * 
	 * @param user
	 *            The requested user.
	 * @param hash
	 *            The requested hashed API token.
	 * @return the API name when there is match between user and API token.
	 */
	@Query(value = "SELECT name FROM SystemApiToken WHERE user=?1 AND hash=?2")
	String findByUserAndHash(String user, String hash);

	/**
	 * Return the token entity matching the name and the user.
	 * 
	 * @param user
	 *            The owner.
	 * @param name
	 *            The API name.
	 * @return Matching entity or <code>null</code>.
	 */
	SystemApiToken findByUserAndName(String user, String name);

	/**
	 * Return all API token names of given user.
	 * 
	 * @param user
	 *            The owner.
	 * @return Owned API token names.
	 */
	@Query(value = "SELECT name FROM SystemApiToken WHERE user=?1 ORDER BY name")
	List<String> findAllByUser(String user);

	/**
	 * Delete the token entity matching the name and the user.
	 * 
	 * @param user
	 *            The owner.
	 * @param name
	 *            The API name.
	 */
	void deleteByUserAndName(String user, String name);
}
