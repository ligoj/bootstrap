/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import org.ligoj.bootstrap.core.json.jqgrid.BasicRule;

/**
 * {@link BasicRule} to {@link Predicate}.
 */
@FunctionalInterface
interface RuleToPredicate {
	/**
	 * Create a {@link Predicate} from a rule's data.
	 * 
	 * @param cb
	 *            The current {@link CriteriaBuilder}
	 * @param data
	 *            The raw data of the related rule.
	 * @param expression
	 *            The current {@link Expression}
	 * @return The predicate. Never <code>null</code>.
	 */
	Predicate toPredicate(EntityManager em, CriteriaBuilder cb, String data, Expression<? extends Comparable<Object>> expression);
}
