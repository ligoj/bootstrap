/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.JoinType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * JAX-RS repository managing standard behaviors of REST.
 *
 * @param <T>
 *            Entity type.
 * @param <K>
 *            Entity's key type.
 */
@NoRepositoryBean
public interface RestRepository<T, K extends Serializable> extends JpaRepository<T, K> {

	/**
	 * Count entities having the given property the expected value.
	 *
	 * @param property
	 *            Property's name.
	 * @param value
	 *            Property's value.
	 * @return The count.
	 */
	long countBy(String property, Object value);

	/**
	 * Delete all entities matching to the given identifiers and return the amount of deleted entities.
	 *
	 * @param identifiers
	 *            The identifier set to delete.
	 * @return the number of deleted entities
	 */
	int deleteAll(Collection<K> identifiers);

	/**
	 * Delete all entities having the given property with the expected value.
	 *
	 * @param property
	 *            property's name.
	 * @param value
	 *            property's value.
	 * @return the amount of deleted entities
	 */
	int deleteAllBy(String property, Object value);

	/**
	 * /** Delete all entities having the given property with the expected value.
	 *
	 * @param property
	 *            property's name.
	 * @param value
	 *            property's value.
	 * @param properties
	 *            Additional property names. Each additional property will correspond to another "AND" clause in the
	 *            initial "WHERE" clause.
	 * @param values
	 *            Additional property values. Each additional values (same amount than properties will correspond to
	 *            another "AND" clause in the initial "WHERE" clause.
	 * @return the amount of deleted entities
	 * @since 2.1.1
	 */
	int deleteAllBy(String property, Object value, String[] properties, Object... values);

	/**
	 * Delete all entities matching to the given identifiers and return the amount of deleted entities. If one or more
	 * entities have not been deleted, a runtime exception is raised.
	 *
	 * @param identifiers
	 *            The identifier set to delete.
	 * @return the number of deleted entities.
	 */
	int deleteAllExpected(Collection<K> identifiers);

	/**
	 * Delete all entities without fetching them from the data base. Warning, the entity manager state will not reflect
	 * this deletion.
	 *
	 * @return the amount of deleted entities
	 */
	int deleteAllNoFetch();

	/**
	 * Delete an entity that must exists and without fetching it from the data base. Warning, the entity manager state
	 * will not reflect this deletion.
	 *
	 * @param id
	 *            entity's identifier.
	 */
	void deleteNoFetch(K id);

	/**
	 * Check the given entity exist.
	 *
	 * @param id
	 *            entity's identifier.
	 */
	void existExpected(K id);

	/**
	 * Search all entities with the given entity with the given property has the expected value. If not found an empty
	 * list is returned.
	 *
	 * @param property
	 *            property's name.
	 * @param value
	 *            property's value.
	 * @return the entities. Never <code>null</code>.
	 */
	List<T> findAllBy(String property, Object value);

	/**
	 * Search all entities with the given entity with the given property has the expected associated value. If not found
	 * an empty list is returned.
	 *
	 * @param property
	 *            property's name.
	 * @param value
	 *            property's value.
	 * @param properties
	 *            Additional property names. Each additional property will correspond to another "AND" clause in the
	 *            initial "WHERE" clause.
	 * @param values
	 *            Additional property values. Each additional values (same amount than properties will correspond to
	 *            another "AND" clause in the initial "WHERE" clause.
	 * @return the entities. Never <code>null</code>.
	 * @since 2.1.1
	 */
	List<T> findAllBy(String property, Object value, String[] properties, Object... values);

	/**
	 * Search an entity having the given property the expected value. If not found a <code>null</code> object is
	 * returned. When several objects are found, only the first one is returned.
	 *
	 * @param property
	 *            Property's name.
	 * @param value
	 *            Property's value.
	 * @return The entity. <code>null</code> when not found.
	 */
	T findBy(String property, Object value);

	/**
	 * Search an entity having the given property the expected value. If not found a <code>null</code> object is
	 * returned. When several objects are found, only the first one is returned.
	 *
	 * @param property
	 *            Property's name.
	 * @param value
	 *            Property's value.
	 * @param properties
	 *            Additional property names. Each additional property will correspond to another "AND" clause in the
	 *            initial "WHERE" clause.
	 * @param values
	 *            Additional property values. Each additional values (same amount than properties will correspond to
	 *            another "AND" clause in the initial "WHERE" clause.
	 * @return The entity. <code>null</code> when not found.
	 * @since 2.1.1
	 */
	T findBy(String property, Object value, String[] properties, Object... values);

	/**
	 * Search an entity with the given entity with the given property has the expected value. If not found a runtime
	 * exception is raised. When several objects are found, only the first one is returned.
	 *
	 * @param property
	 *            property's name.
	 * @param value
	 *            property's value.
	 * @return the entity. <code>null</code> when not found.
	 */
	T findByExpected(String property, Object value);

	/**
	 * Search an entity with the given entity with the given name. If not found a <code>null</code> object is returned.
	 * When several objects are found, only the first one is returned.
	 *
	 * @param name
	 *            entity's name.
	 * @return the entity. <code>null</code> when not found.
	 */
	T findByName(String name);

	/**
	 * Search an entity with the given entity with the given name. If not found a runtime exception is raised. When
	 * several objects are found, only the first one is returned.
	 *
	 * @param name
	 *            entity's name.
	 * @return the entity.
	 */
	T findByNameExpected(String name);

	/**
	 * Retrieves an entity by its id.
	 *
	 * @param id
	 *            must not be {@literal null}.
	 * @return the entity with the given id or {@literal Optional#empty()} if none found
	 * @throws IllegalArgumentException
	 *             if {@code id} is {@literal null}.
	 */
	default T findOne(final K id) {
		return findById(id).orElse(null);
	}

	/**
	 * Search an expected entity with the given identifier. If not found a runtime exception is raised.
	 *
	 * @param id
	 *            entity's identifier.
	 * @return the non <code>null</code> entity.
	 */
	T findOneExpected(K id);

	/**
	 * Search an expected entity with the given identifier with fetched associations. If not found a runtime exception
	 * is raised. When several objects are found, only the first one is returned.
	 *
	 * @param id
	 *            entity's identifier.
	 * @param fetchedAssociations
	 *            A map of association to fetch. The map keys for composites associations should not have two times the
	 *            same identifier &lt;"contrat.contrat", JoinType.INNER&gt; is not possible although
	 *            &lt;"contrats.contrat", JoinType.INNER&gt; is accepted.
	 * @return the non <code>null</code> entity.
	 */
	T findOneExpected(K id, Map<String, JoinType> fetchedAssociations);
}