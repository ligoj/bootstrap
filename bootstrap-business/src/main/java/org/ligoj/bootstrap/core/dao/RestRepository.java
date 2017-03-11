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
 * @param <ID>
 *            Entity's key type.
 */
@NoRepositoryBean
public interface RestRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

	/**
	 * Search an expected entity with the given identifier. If not found a runtime exception is raised.
	 * 
	 * @param id
	 *            entity's identifier.
	 * @return the non <code>null</code> entity.
	 */
	T findOneExpected(ID id);

	/**
	 * Check the given entity exist.
	 * 
	 * @param id
	 *            entity's identifier.
	 */
	void existExpected(ID id);

	/**
	 * Delete an entity that must exists.
	 * 
	 * @param id
	 *            entity's identifier.
	 */
	@Override
	void delete(ID id);

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
	 * Delete an entity that must exists and without fetching it from the data base. Warning, the entity manager state
	 * will not reflect this deletion.
	 * 
	 * @param id
	 *            entity's identifier.
	 */
	void deleteNoFetch(ID id);

	/**
	 * Delete all entities without fetching them from the data base. Warning, the entity manager state will not reflect
	 * this
	 * deletion.
	 * 
	 * @return the amount of deleted entities
	 */
	int deleteAllNoFetch();

	/**
	 * Delete all entities matching to the given identifiers and return the amount of deleted entities.
	 * 
	 * @return the number of deleted entities
	 */
	int deleteAll(Collection<ID> identifiers);

	/**
	 * Delete all entities matching to the given identifiers and return the amount of deleted entities. If one or more
	 * entities have not been deleted, a runtime exception
	 * is raised.
	 * 
	 * @return the number of deleted entities.
	 */
	int deleteAllExpected(Collection<ID> identifiers);

	/**
	 * Search an expected entity with the given identifier with fetched associations. If not found a runtime exception
	 * is raised. When several objects are found, only the first one is returned.
	 * 
	 * @param id
	 *            entity's identifier.
	 * @param fetchedAssociations
	 *            A map of association to fetch. The map keys for composites associations should not have two times the
	 *            same identifier &lt;"contrat.contrat", JoinType.INNER&gt; is not possible although
	 *            &lt;"contrats.contrat",
	 *            JoinType.INNER&gt; is accepted.
	 * @return the non <code>null</code> entity.
	 */
	T findOneExpected(ID id, Map<String, JoinType> fetchedAssociations);

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
	 * Search an entity with the given entity with the given property has the expected value. If not found a null object
	 * is returned. When several objects are found, only the first one is returned.
	 * 
	 * @param property
	 *            property's name.
	 * @param value
	 *            property's value.
	 * @return the entity. <code>null</code> when not found.
	 */
	T findBy(String property, Object value);

	/**
	 * Search all entities with the given entity with the given property has the expected value. If not found a null
	 * object
	 * is returned.
	 * 
	 * @param property
	 *            property's name.
	 * @param value
	 *            property's value.
	 * @return the entities. Never <code>null</code>.
	 */
	List<T> findAllBy(String property, Object value);

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
}