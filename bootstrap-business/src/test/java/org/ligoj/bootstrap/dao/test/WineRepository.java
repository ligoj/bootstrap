package org.ligoj.bootstrap.dao.test;

import java.util.List;

import javax.persistence.OrderBy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import org.ligoj.bootstrap.core.dao.RestRepository;
import org.ligoj.bootstrap.model.test.Wine;

/**
 * Wines , JDBC based DAO
 */
public interface WineRepository extends RestRepository<Wine, Integer> {

	/**
	 * Return all {@link Wine} objects with the given name.
	 * 
	 * @param name
	 *            the {@link Wine} name to match.
	 * @return all {@link Wine} objects with the given name. Insensitive case search is used.
	 */
	@OrderBy("name")
	List<Wine> findByNameContainingIgnoreCase(String name);

	/**
	 * Return all {@link Wine} objects.
	 * 
	 * @param pageable
	 *            the pagination request.
	 * @return All {@link Wine} objects.
	 */
	@Query("FROM Wine w")
	Page<Wine> findAllQueryAlias(Pageable pageable);

}
