/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import java.util.List;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.system.SystemApiToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * API token repository.
 */
public interface SystemApiTokenRepository extends RestRepository<SystemApiToken, Integer> {

	String NOT_EXPIRED = " AND (expiration IS NULL OR expiration > CURRENT_TIMESTAMP)";

	/**
	 * Return the name of the token matching the user.
	 *
	 * @param user The requested user.
	 * @param hash The requested hashed API token.
	 * @return <code>true</code> when there is match between user and API token.
	 */
	@Query("SELECT CASE WHEN count(user) > 0 THEN true ELSE false END "
			+ "FROM SystemApiToken WHERE user=:user AND hash=:hash"
			+ NOT_EXPIRED)
	boolean checkByUserAndHash(String user, String hash);

	/**
	 * Return the name of the token matching the user. Used for plain text token, without hash.
	 *
	 * @param user  The requested user.
	 * @param token The requested plain text/unsecured API token.
	 * @return <code>true</code> when there is match between user and API token.
	 */
	@Query("SELECT CASE WHEN count(user) > 0 THEN true ELSE false END "
			+ "FROM SystemApiToken WHERE user=:user AND hash ='_plain_' AND token=:token"
			+ NOT_EXPIRED)
	boolean checkByUserAndToken(String user, String token);

	/**
	 * Return the token entity matching the name and the user.
	 *
	 * @param user The owner.
	 * @param name The API name.
	 * @return Matching entity or <code>null</code>.
	 */
	@Query("SELECT t FROM SystemApiToken t WHERE user=:user AND name=:name" + NOT_EXPIRED)
	SystemApiToken findByUserAndName(String user, String name);

	/**
	 * Return all API token names of given user.
	 *
	 * @param user The owner.
	 * @return Owned API token names.
	 */
	@Query("SELECT name FROM SystemApiToken WHERE user=:user" + NOT_EXPIRED + " ORDER BY name")
	List<String> findAllByUser(String user);

	/**
	 * Delete the token entity matching the name and the user.
	 *
	 * @param user The owner.
	 * @param name The API name.
	 */
	void deleteByUserAndName(String user, String name);

	/**
	 * Delete all expired API tokens
	 * Return deleted of deleted API tokens.
	 */
	@Query("DELETE FROM SystemApiToken WHERE expiration IS NOT NULL AND expiration < CURRENT_TIMESTAMP")
	@Modifying
	int deleteExpired();

	/**
	 * Delete all expired API tokens owned by given user.
	 * Return deleted of deleted API tokens.
	 */
	@Query("DELETE FROM SystemApiToken WHERE expiration IS NOT NULL AND user=:user AND expiration < CURRENT_TIMESTAMP")
	@Modifying
	int deleteExpired(final String user);
}
