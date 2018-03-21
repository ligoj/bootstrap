/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections4.MapUtils;
import org.ligoj.bootstrap.core.json.jqgrid.UiPageRequest;
import org.ligoj.bootstrap.core.json.jqgrid.UiSort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * General JPA entity Pagination DAO
 * 
 * @author Fabrice Daugan
 */
@Repository
@Transactional(TxType.MANDATORY)
public class PaginationDao {

	@PersistenceContext(unitName = "pu")
	private EntityManager em;

	@Autowired
	private FetchHelper fetchHelper;

	/**
	 * Get list of JPA entities mapped and paginate
	 * 
	 * @param <T>
	 *            JPA entity type to fetch.
	 * @param entityType
	 *            JPA entity class to fetch.
	 * @param uiPageRequest
	 *            the page request containing filters, and sorts.
	 * @param mapping
	 *            the JSON to SQL mapping. Property access is allowed using '.' separator.
	 * @return a list of JPA entities matching the given filter. The result is paginated and filtered.
	 */
	public <T> Page<T> findAll(final Class<T> entityType, final UiPageRequest uiPageRequest, final Map<String, String> mapping) {
		return findAll(entityType, uiPageRequest, mapping, null);
	}

	/**
	 * Get a list of JPA entities matching the given filter. The result is paginated and filtered.
	 * 
	 * @param <T>
	 *            JPA entity type to fetch.
	 * @param entityType
	 *            JPA entity class to fetch.
	 * @param uiPageRequest
	 *            the page request containing filters, and sorts.
	 * @param mapping
	 *            the JSON to SQL mapping. Property access is allowed using '.' separator.
	 * @param fetchedAssociations
	 *            A map of association to fetch. The map keys for composites associations should not have two times the
	 *            same identifier &lt;"contrat.contrat", JoinType.INNER&gt; is not possible although
	 *            &lt;"contrats.contrat", JoinType.INNER&gt; is accepted.
	 * @return a list of JPA entities matching the given filter. The result is paginated and filtered.
	 */
	public <T> Page<T> findAll(final Class<T> entityType, final UiPageRequest uiPageRequest, final Map<String, String> mapping,
			final Map<String, JoinType> fetchedAssociations) {
		return findAll(entityType, uiPageRequest, mapping, Collections.emptyMap(), fetchedAssociations);
	}

	/**
	 * Get a list of JPA entities matching the given filter. The result is paginated and filtered.
	 * 
	 * @param <T>
	 *            JPA entity type to fetch.
	 * @param entityType
	 *            JPA entity class to fetch.
	 * @param uiPageRequest
	 *            the page request containing filters, and sorts.
	 * @param mapping
	 *            the JSON to SQL mapping. Property access is allowed using '.' separator.
	 * @param specifications
	 *            the optional custom specification mapping.
	 * @param fetchedAssociations
	 *            A map of association to fetch. The map keys for composites associations should not have two times the
	 *            same identifier &lt;"contrat.contrat", JoinType.INNER&gt; is not possible although
	 *            &lt;"contrats.contrat", JoinType.INNER&gt; is accepted.
	 * @return a list of JPA entities matching the given filter. The result is paginated and filtered.
	 */
	public <T> Page<T> findAll(final Class<T> entityType, final UiPageRequest uiPageRequest, final Map<String, String> mapping,
			final Map<String, CustomSpecification> specifications, final Map<String, JoinType> fetchedAssociations) {

		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(entityType);
		final Specification<T> spec = newSpecification(uiPageRequest, mapping, specifications);

		// Apply specification
		final Root<T> root = query.from(entityType);
		if (!CollectionUtils.isEmpty(fetchedAssociations)) {
			fetchHelper.applyFetchedAssociations(fetchedAssociations, root);
		}

		applySpecificationToCriteria(root, spec, query);
		query.select(root);
		applyOrder(uiPageRequest, MapUtils.emptyIfNull(mapping), builder, query, root);
		return pagedResult(entityType, uiPageRequest, query, spec);
	}

	/**
	 * Apply ordering criteria.
	 */
	private <T> void applyOrder(final UiPageRequest uiPageRequest, final Map<String, String> mapping, final CriteriaBuilder builder,
			final CriteriaQuery<T> query, final Root<T> root) {
		// Apply the sort
		if (uiPageRequest.getUiSort() != null) {

			// Need to order the result
			final UiSort uiSort = uiPageRequest.getUiSort();
			final String ormColumn = mapping.get(uiSort.getColumn());
			if (ormColumn != null) {

				// ORM column is validated
				final Sort sort = new Sort(uiSort.getDirection(), mapping.get(uiSort.getColumn()));
				query.orderBy(QueryUtils.toOrders(sort, root, builder));
			}
		}
	}

	/**
	 * Apply pagination criteria.
	 */
	private <T> Page<T> pagedResult(final Class<T> entityType, final UiPageRequest uiPageRequest, final CriteriaQuery<T> query,
			final Specification<T> spec) {
		final TypedQuery<T> query2 = em.createQuery(query);
		if (uiPageRequest.getPage() > 0 && uiPageRequest.getPageSize() > 0 || uiPageRequest.getUiSort() != null) {
			// Build the main query
			final Pageable pageable = PageRequest.of(Math.max(0, uiPageRequest.getPage() - 1), Math.max(1, uiPageRequest.getPageSize()));
			return readPage(entityType, query2, pageable, spec);
		}

		// Build the main query
		return new PageImpl<>(query2.getResultList());
	}

	/**
	 * Return the specification corresponding to the given filter.
	 * 
	 * @param <U>
	 *            Any entity type.
	 * @param uiPageRequest
	 *            the page request containing filters, and sorts.
	 * @param mapping
	 *            the JSON to SQL mapping. Property access is allowed using '.' separator.
	 * @param specifications
	 *            the custom specifications.
	 * @return the specification corresponding to the given filter.
	 */
	public <U> Specification<U> newSpecification(final UiPageRequest uiPageRequest, final Map<String, String> mapping,
			final Map<String, CustomSpecification> specifications) {
		if (uiPageRequest.getUiFilter() == null || uiPageRequest.getUiFilter().getRules() == null
				|| uiPageRequest.getUiFilter().getRules().isEmpty()) {
			// No filter implies no specification
			return null;
		}

		// Return a specification based on a set of UI filter
		return new DynamicSpecification<>(uiPageRequest.getUiFilter(), mapping, specifications);
	}

	/**
	 * Reads the given {@link TypedQuery} into a {@link Page} applying the given {@link Pageable} and
	 * {@link Specification}.
	 */
	private <T> Page<T> readPage(final Class<T> entityType, final TypedQuery<T> query, final Pageable pageable, final Specification<T> spec) {

		query.setFirstResult((int)pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());

		final Long total = getCountQuery(entityType, spec).getSingleResult();

		return new PageImpl<>(query.getResultList(), pageable, total);
	}

	/**
	 * Creates a new count query for the given {@link Specification}.
	 */
	private <T> TypedQuery<Long> getCountQuery(final Class<T> entityType, final Specification<T> spec) {

		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Long> query = builder.createQuery(Long.class);

		final Root<T> root = query.from(entityType);
		applySpecificationToCriteria(root, spec, query);
		query.select(builder.count(root));

		return em.createQuery(query);
	}

	/**
	 * Applies the given {@link Specification} to the given {@link CriteriaQuery}.
	 */
	private <S, T> Root<T> applySpecificationToCriteria(final Root<T> root, final Specification<T> spec, final CriteriaQuery<S> query) {

		Assert.notNull(query, "Query is requested");

		if (spec != null) {
			// There is at least one described filter
			final CriteriaBuilder builder = em.getCriteriaBuilder();
			final Predicate predicate = spec.toPredicate(root, query, builder);

			// There is at least one validated filter
			query.where(predicate);
		}
		return root;
	}

}
