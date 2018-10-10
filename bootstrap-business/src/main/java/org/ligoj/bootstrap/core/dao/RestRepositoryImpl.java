/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.SpringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of {@link RestRepository}.
 *
 * @param <T>
 *            Entity type.
 * @param <K>
 *            Entity's key type.
 */
public class RestRepositoryImpl<T, K extends Serializable> extends SimpleJpaRepository<T, K>
		implements RestRepository<T, K> {

	/**
	 * Entity manager, only there because of ugly design of Spring Data.
	 */
	private final EntityManager em;
	private final JpaEntityInformation<T, ?> ei;
	private static final String SELECT_BY = "FROM %s WHERE ";
	private static final String DELETE_ALL = "DELETE %s";
	private static final String DELETE_BY = DELETE_ALL + " WHERE ";
	private static final String DELETE_ALL_IN = DELETE_ALL + " WHERE %s IN (:%s)";
	private static final String PARAM_VALUE = "value";

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
	public T findOneExpected(final K id) {
		return findOneExpected(id, null);
	}

	@Override
	public T findOneExpected(final K id, final Map<String, JoinType> fetchedAssociations) {
		final T entity;
		if (CollectionUtils.isEmpty(fetchedAssociations)) {
			entity = findOne(id);
		} else {
			entity = findOne(id, fetchedAssociations);
		}
		return Optional.ofNullable(entity).orElseThrow(() -> new EntityNotFoundException(id.toString()));
	}

	/**
	 * Find one with fetched associations.
	 */
	private T findOne(final K id, final Map<String, JoinType> fetchedAssociations) {
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(getDomainClass());

		// Apply fetch
		final Root<T> root = query.from(getDomainClass());
		SpringUtils.getBean(FetchHelper.class).applyFetchedAssociations(fetchedAssociations, root);

		// Apply specification
		final Specification<T> specification = (r, q, cb) -> cb.equal(r.get("id"), id);
		query.where(specification.toPredicate(root, query, builder));
		query.select(root);
		return em.createQuery(query).getSingleResult();
	}

	@Override
	@Transactional
	public void deleteNoFetch(final K id) {
		if (update(DELETE_BY, "id", id, ArrayUtils.EMPTY_STRING_ARRAY) != 1) {
			// No deleted row
			throw new EntityNotFoundException(id.toString());
		}
		// Exactly one row has been deleted has expected
	}

	@Override
	@Transactional
	public int deleteAll(final Collection<K> identifiers) {
		if (org.apache.commons.collections4.CollectionUtils.isEmpty(identifiers)) {
			return 0;
		}
		return update(DELETE_ALL_IN, "id", identifiers, ArrayUtils.EMPTY_STRING_ARRAY);
	}

	@Override
	@Transactional
	public int deleteAllExpected(final Collection<K> identifiers) {
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
	public void existExpected(final K id) {
		if (!existsById(id)) {
			throw new EntityNotFoundException(id.toString());
		}
	}

	@Override
	public T findBy(final String property, final Object value) {
		return findBy(property, value, ArrayUtils.EMPTY_STRING_ARRAY);
	}

	@Override
	public T findBy(final String property, final Object value, final String[] properties, final Object... values) {
		// No result, null return
		return newQuery(SELECT_BY, property, value, properties, values).setMaxResults(1).getResultList().stream()
				.findFirst().orElse(null);
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
		return Optional.ofNullable(findBy(property, value))
				.orElseThrow(() -> new EntityNotFoundException(String.valueOf(value)));
	}

	@Override
	@Transactional
	public int deleteAllBy(final String property, final Object value) {
		return deleteAllBy(property, value, ArrayUtils.EMPTY_STRING_ARRAY);
	}

	@Override
	public int deleteAllBy(String property, Object value, String[] properties, Object... values) {
		return update(DELETE_BY, property, value, properties, values);
	}

	@Override
	public List<T> findAllBy(final String property, final Object value) {
		return findAllBy(property, value, ArrayUtils.EMPTY_STRING_ARRAY);
	}

	@Override
	public List<T> findAllBy(final String property, final Object value, final String[] properties,
			final Object... values) {
		return newQuery(SELECT_BY, property, value, properties, values).getResultList();
	}

	/**
	 * Generate a new {@link TypedQuery} from the base pattern, with initial WHERE constraint
	 * <code>property=:value</code> and some optional more constraints pairs.
	 */
	private TypedQuery<T> newQuery(final String patternQuery, final String property, final Object value,
			final String[] properties, final Object... values) {
		final StringBuilder baseQuery = newQueryString(patternQuery, property, value, properties, values);
		return addParameters(em.createQuery(baseQuery.toString(), ei.getJavaType()), value, values);
	}

	/**
	 * Generate a new {@link Query} from the base pattern, with initial WHERE constraint <code>property=:value</code>
	 * and some optional more constraints pairs.
	 */
	private int update(final String patternQuery, final String property, final Object value, final String[] properties,
			final Object... values) {
		final StringBuilder baseQuery = newQueryString(patternQuery, property, value, properties, values);
		return addParameters(em.createQuery(baseQuery.toString()), value, values).executeUpdate();
	}

	@Override
	public long countBy(String property, Object value) {
		return em.createQuery(String.format("SELECT COUNT(*) FROM %s WHERE %s=:value", ei.getEntityName(), property),
				Long.class).setParameter(PARAM_VALUE, value).getSingleResult();
	}

	/**
	 * Create a new query buffer from a "... FROM %s WHERE " or "FROM %s WHERE %s .. %s" alike template string and add
	 * this filters (<code>AND</code>).
	 */
	private StringBuilder newQueryString(final String patternQuery, final String property, final Object value,
			final String[] properties, final Object... values) {
		final StringBuilder baseQuery;
		if (StringUtils.countMatches(patternQuery, "%s") == 1) {
			// Only "FROM"
			baseQuery = new StringBuilder(String.format(patternQuery, ei.getEntityName()));
			addFilter(baseQuery, property, value, 0);
		} else {
			// "FROM WHERE property ... value
			baseQuery = new StringBuilder(String.format(patternQuery, ei.getEntityName(), property, PARAM_VALUE + 0));
		}
		for (int index = 0; index < values.length; index++) {
			baseQuery.append(" AND ");
			addFilter(baseQuery, properties[index], values[index], index + 1);
		}
		return baseQuery;
	}

	/**
	 * Add a query filter to the buffer. Handle the <code>null</code> value case with a <code>IS NULL</code>
	 */
	private void addFilter(final StringBuilder baseQuery, final String property, final Object value, final int index) {
		baseQuery.append(property);
		if (value == null) {
			baseQuery.append(" IS NULL");
		} else {
			baseQuery.append("=:");
			baseQuery.append(PARAM_VALUE);
			baseQuery.append(String.valueOf(index));
		}
	}

	/**
	 * Add query parameters, handling the <code>null</code> value case.
	 */
	private <Q extends Query> Q addParameters(final Q query, final Object value, final Object... values) {
		addParameter(query, value, 0);
		for (int index = 0; index < values.length; index++) {
			addParameter(query, values[index], index + 1);
		}
		return query;
	}

	/**
	 * Add a query parameter, handling the <code>null</code> value case.
	 */
	private void addParameter(final Query query, final Object value, final int index) {
		if (value != null) {
			query.setParameter(PARAM_VALUE + index, value);
		}
	}
}