/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections4.MapUtils;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule.RuleOperator;
import org.ligoj.bootstrap.core.json.jqgrid.UIRule;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter.FilterOperator;
import org.springframework.data.jpa.domain.Specification;

import lombok.extern.slf4j.Slf4j;

/**
 * A specification managing multiple rules, grouping, ordering and fetching.
 * 
 * @param <U>
 *            Attached entity type.
 */
@Slf4j
class DynamicSpecification<U> extends AbstractSpecification implements Specification<U> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Property delimiter. See Property#DELIMITERS property.
	 */
	public static final String PROPERTY_DELIMITERS = "_\\.";

	/**
	 * Wild card char for "like" operator.
	 */
	private static final String LIKE = "%";

	/**
	 * Mapper to build a {@link Predicate} from data, expression and criteria builder.
	 */
	private static final EnumMap<RuleOperator, RuleToPredicate> PREDIACATE_MAPPER = new EnumMap<>(RuleOperator.class);
	static {
		PREDIACATE_MAPPER.put(RuleOperator.BW, (cb, d, e) -> cb.like(cb.upper(e.as(String.class)), d.toUpperCase(Locale.ENGLISH) + LIKE));
		PREDIACATE_MAPPER.put(RuleOperator.CN, (cb, d, e) -> cb.like(cb.upper(e.as(String.class)), LIKE + d.toUpperCase(Locale.ENGLISH) + LIKE));
		PREDIACATE_MAPPER.put(RuleOperator.EW, (cb, d, e) -> cb.like(cb.upper(e.as(String.class)), LIKE + d.toUpperCase(Locale.ENGLISH)));
		PREDIACATE_MAPPER.put(RuleOperator.GT, (cb, d, e) -> cb.greaterThan(e, toRawData(d, e)));
		PREDIACATE_MAPPER.put(RuleOperator.GTE, (cb, d, e) -> cb.greaterThanOrEqualTo(e, toRawData(d, e)));
		PREDIACATE_MAPPER.put(RuleOperator.LT, (cb, d, e) -> cb.lessThan(e, toRawData(d, e)));
		PREDIACATE_MAPPER.put(RuleOperator.LTE, (cb, d, e) -> cb.lessThanOrEqualTo(e, toRawData(d, e)));
		PREDIACATE_MAPPER.put(RuleOperator.NE, (cb, d, e) -> cb.notEqual(e, toRawData(d, e)));
		PREDIACATE_MAPPER.put(RuleOperator.EQ, (cb, d, e) -> cb.equal(e, toRawData(d, e)));
	}

	/**
	 * JQ Grid filters.
	 */
	private final UiFilter filter;

	/**
	 * Business to ORM field mapping.
	 */
	private final Map<String, String> mapping;

	/**
	 * Business to ORM field mapping.
	 */
	private final Map<String, CustomSpecification> specifications;

	/**
	 * Set the filter configurations.
	 * 
	 * @param filter
	 *            the filters.
	 * @param mapping
	 *            the mapping used to match JSON properties/path with the ORM path.
	 * @param specifications
	 *            the custom specifications.
	 */
	DynamicSpecification(final UiFilter filter, final Map<String, String> mapping, final Map<String, CustomSpecification> specifications) {
		this.filter = filter;
		this.mapping = MapUtils.emptyIfNull(mapping);
		this.specifications = MapUtils.emptyIfNull(specifications);
	}

	/**
	 * Return a custom predicate.
	 */
	private Predicate getCustomPredicate(final Root<U> root, final CriteriaBuilder cb, final BasicRule rule, final CriteriaQuery<?> query) {
		final CustomSpecification specification = specifications.get(rule.getField());
		if (specification == null) {
			// Invalid path
			log.error("A CustomSpecification must be defined when custom operator type ('ct') is used");
			// no specification, ignore it...
			return null;
		}
		return specification.toPredicate(root, query, cb, rule);
	}

	/**
	 * Create a predicate group.
	 */
	private Predicate getGroupPredicate(final UiFilter group, final Root<U> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
		// Build the predicates
		final java.util.List<Predicate> predicates = getPredicates(group, root, query, cb);

		// Build the specification
		if (predicates.isEmpty()) {
			return cb.conjunction();
		}
		final Predicate[] filteredPredicates = predicates.toArray(new Predicate[predicates.size()]);
		if (group.getGroupOp() == FilterOperator.AND) {
			return cb.and(filteredPredicates);
		}
		return cb.or(filteredPredicates);
	}

	/**
	 * Return the ORM path from the given rule.
	 */
	private <T> Path<T> getOrmPath(final Root<U> root, final BasicRule rule) {
		final String path = mapping.getOrDefault(rule.getField(), mapping.containsKey("*") ? rule.getField() : null);
		if (path == null) {
			// Invalid path, coding issue or SQL injection attempt
			log.error(String.format("Non mapped property '%s' found for entity class '%s'", rule.getField(), root.getJavaType().getName()));
			return null;
		}
		return getOrmPath(root, path);
	}

	/**
	 * Return the predicate corresponding to the given rule.
	 */
	private <X extends Comparable<Object>> Predicate getPredicate(final CriteriaBuilder cb, final BasicRule rule, final Expression<X> expression) {
		return PREDIACATE_MAPPER.get(rule.getOp()).toPredicate(cb, rule.getData(), expression);
	}

	/**
	 * Return a predicate from a rule.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Predicate getPredicate(final Root<U> root, final CriteriaBuilder cb, final BasicRule rule, final CriteriaQuery<?> query) {
		if (rule.getOp() == RuleOperator.CT) {
			return getCustomPredicate(root, cb, rule, query);
		}
		final Expression expression = getOrmPath(root, rule);
		if (expression == null) {
			// Non matched expression, ignore it...
			log.info(String.format("SQL injection attack ? Unable to map request rule for property %s", rule.getField()));
			return null;
		}
		return getPredicate(cb, rule, expression);
	}

	/**
	 * Return the predicate corresponding the given rule.
	 */
	private Predicate getPredicate(final Root<U> root, final CriteriaQuery<?> query, final CriteriaBuilder cb, final UIRule rule) {
		final Predicate predicate;
		if (rule instanceof BasicRule) {
			predicate = getPredicate(root, cb, (BasicRule) rule, query);
		} else {
			predicate = getGroupPredicate((UiFilter) rule, root, query, cb);
		}
		return predicate;
	}

	/**
	 * Return the predicates list.
	 */
	private java.util.List<Predicate> getPredicates(final UiFilter group, final Root<U> root, final CriteriaQuery<?> query,
			final CriteriaBuilder cb) {
		final java.util.List<Predicate> predicates = new ArrayList<>(filter.getRules().size());
		for (final UIRule rule : group.getRules()) {
			final Predicate predicate = getPredicate(root, query, cb, rule);
			if (predicate != null) {
				predicates.add(predicate);
			}
		}
		return predicates;
	}

	@Override
	public Predicate toPredicate(final Root<U> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
		return getGroupPredicate(filter, root, query, cb);
	}
}
