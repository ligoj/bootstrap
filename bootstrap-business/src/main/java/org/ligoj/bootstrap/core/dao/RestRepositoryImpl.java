package org.ligoj.bootstrap.core.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import org.ligoj.bootstrap.core.SpringUtils;

/**
 * Implementation of {@link RestRepository}.
 * 
 * @param <T>
 *            Entity type.
 * @param <ID>
 *            Entity's key type.
 */
public class RestRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements RestRepository<T, ID> {

	/**
	 * Entity manager, only there because of ugly design of Spring Data.
	 */
	private final EntityManager em;
	private final JpaEntityInformation<T, ?> ei;
	private static final String SELECT_BY = "FROM %s WHERE %s=:value";
	private static final String DELETE_ALL = "DELETE %s";
	private static final String DELETE_BY = DELETE_ALL + " WHERE %s=:value";
	private static final String DELETE_ALL_IN = "DELETE %s WHERE id IN (:ids)";

	/**
	 * Creates a new {@link RestRepositoryImpl} to manage objects of the given {@link JpaEntityInformation}.
	 * 
	 * @param entityInformation
	 *            must not be {@literal null}.
	 * @param entityManager
	 *            must not be {@literal null}.
	 */
	public RestRepositoryImpl(final JpaEntityInformation<T, ?> entityInformation, final EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.ei = entityInformation;
		this.em = entityManager;
	}

	@Override
	public T findOneExpected(final ID id) {
		return findOneExpected(id, null);
	}

	@Override
	public T findOneExpected(final ID id, final Map<String, JoinType> fetchedAssociations) {
		final T entity;
		if (CollectionUtils.isEmpty(fetchedAssociations)) {
			entity = findOne(id);
		} else {
			entity = findOne(id, fetchedAssociations);
		}

		if (entity == null) {
			throw new EntityNotFoundException(id.toString());
		}
		return entity;
	}

	/**
	 * Find one with fetched associations.
	 */
	private T findOne(final ID id, final Map<String, JoinType> fetchedAssociations) {
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(getDomainClass());

		// Apply fetch
		final Root<T> root = query.from(getDomainClass());
		SpringUtils.getBean(FetchHelper.class).applyFetchedAssociations(fetchedAssociations, root);

		// Apply specification
		final Predicate predicate = (new Specification<T>() {

			@Override
			public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.equal(root.get("id"), id);
			}
		}).toPredicate(root, query, builder);
		query.where(predicate);
		query.select(root);
		return em.createQuery(query).getSingleResult();
	}

	@Override
	@Transactional
	public void delete(final ID id) {
		delete(findOneExpected(id));
	}

	@Override
	@Transactional
	public void deleteNoFetch(final ID id) {
		if (em.createQuery(String.format(DELETE_BY, ei.getEntityName(), "id")).setParameter("value", id).executeUpdate() != 1) {
			// No deleted row
			throw new EntityNotFoundException(id.toString());
		}
		// Exactly one row has been deleted has expected
	}

	@Override
	@Transactional
	public int deleteAll(final Collection<ID> identifiers) {
		if (org.apache.commons.collections4.CollectionUtils.isEmpty(identifiers)) {
			return 0;
		}
		return em.createQuery(String.format(DELETE_ALL_IN, ei.getEntityName())).setParameter("ids", identifiers).executeUpdate();
	}

	@Override
	@Transactional
	public int deleteAllExpected(final Collection<ID> identifiers) {
		final int deleted = deleteAll(identifiers);
		if (deleted != org.apache.commons.collections4.CollectionUtils.size(identifiers)) {
			// At least one row has not been deleted
			throw new EntityNotFoundException(identifiers.toString());
		}
		return deleted;
	}

	@Override
	@Transactional
	public int deleteAllNoFetch() {
		return em.createQuery(String.format(DELETE_ALL, ei.getEntityName())).executeUpdate();
	}

	@Override
	public void existExpected(final ID id) {
		if (!exists(id)) {
			throw new EntityNotFoundException(id.toString());
		}
	}

	@Override
	public T findBy(final String property, final Object value) {
		final List<T> resultList = em.createQuery(String.format(SELECT_BY, ei.getEntityName(), property), ei.getJavaType())
				.setParameter("value", value).setMaxResults(1).getResultList();
		if (resultList.isEmpty()) {
			// No result, null return
			return null;
		}
		return resultList.get(0);
	}

	@Override
	public T findByName(final String name) {
		return findBy("name", name);
	}

	@Override
	public T findByNameExpected(final String name) {
		return findByExpected("name", name);
	}

	@Override
	public T findByExpected(final String property, final Object value) {
		final T entity = findBy(property, value);
		if (entity == null) {
			throw new EntityNotFoundException(String.valueOf(value));
		}
		return entity;
	}

	@Override
	@Transactional
	public int deleteAllBy(final String property, final Object value) {
		return em.createQuery(String.format(DELETE_BY, ei.getEntityName(), property)).setParameter("value", value).executeUpdate();
	}

	@Override
	public List<T> findAllBy(final String property, final Object value) {
		return em.createQuery(String.format(SELECT_BY, ei.getEntityName(), property), ei.getJavaType()).setParameter("value", value).getResultList();
	}

}