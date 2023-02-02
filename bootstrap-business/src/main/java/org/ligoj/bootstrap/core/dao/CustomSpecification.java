/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.ligoj.bootstrap.core.json.jqgrid.BasicRule;

/**
 * Custom specification template used to math a UI requirement with the JPA {@link Predicate}
 */
@FunctionalInterface
public interface CustomSpecification {

	/**
	 * Creates a WHERE clause for a query of the referenced entity in form of a {@link Predicate} for the given
	 * {@link Root} and {@link CriteriaQuery}.
	 * 
	 * @param root
	 *            root
	 * @param query
	 *            query
	 * @param cb
	 *            the criteria builder
	 * @param rule
	 *            the rule to transform.
	 * @return a {@link Predicate}, must not be {@literal null}.
	 */
	Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb, BasicRule rule);
}
