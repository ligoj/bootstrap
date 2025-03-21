/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule;
import org.ligoj.bootstrap.core.json.jqgrid.BasicRule.RuleOperator;
import org.ligoj.bootstrap.core.json.jqgrid.UIRule;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter;
import org.ligoj.bootstrap.core.json.jqgrid.UiFilter.FilterOperator;
import org.springframework.data.jpa.domain.Specification;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A specification managing multiple rules, grouping, ordering and fetching.
 *
 * @param <U> Attached entity type.
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
	private static final EnumMap<RuleOperator, RuleToPredicate> PREDICATE_MAPPER = new EnumMap<>(RuleOperator.class);

	static {
		PREDICATE_MAPPER.put(RuleOperator.BW,
				(em, cb, d, e) -> cb.like(cb.upper(e.as(String.class)), d.toUpperCase(Locale.ENGLISH) + LIKE));
		PREDICATE_MAPPER.put(RuleOperator.CN,
				(em, cb, d, e) -> cb.like(cb.upper(e.as(String.class)), LIKE + d.toUpperCase(Locale.ENGLISH) + LIKE));
		PREDICATE_MAPPER.put(RuleOperator.EW,
				(em, cb, d, e) -> cb.like(cb.upper(e.as(String.class)), LIKE + d.toUpperCase(Locale.ENGLISH)));
		PREDICATE_MAPPER.put(RuleOperator.GT, (em, cb, d, e) -> cb.greaterThan(e, toRawData(em, d, e)));
		PREDICATE_MAPPER.put(RuleOperator.GTE, (em, cb, d, e) -> cb.greaterThanOrEqualTo(e, toRawData(em, d, e)));
		PREDICATE_MAPPER.put(RuleOperator.LT, (em, cb, d, e) -> cb.lessThan(e, toRawData(em, d, e)));
		PREDICATE_MAPPER.put(RuleOperator.LTE, (em, cb, d, e) -> cb.lessThanOrEqualTo(e, toRawData(em, d, e)));
		PREDICATE_MAPPER.put(RuleOperator.NE, (em, cb, d, e) -> cb.notEqual(e, toRawData(em, d, e)));
		PREDICATE_MAPPER.put(RuleOperator.EQ, (em, cb, d, e) -> cb.equal(e, toRawData(em, d, e)));
	}

	/**
	 * Used to resolve actual data types.
	 */
	private final transient EntityManager em;

	/**
	 * JQ Grid filters.
	 */
	private final transient UiFilter filter;

	/**
	 * Business to ORM field mapping.
	 */
	private final transient Map<String, String> mapping;

	/**
	 * Business to ORM field mapping.
	 */
	private final transient Map<String, CustomSpecification> specifications;

	/**
	 * Set the filter configurations.
	 *
	 * @param filter         the filters.
	 * @param mapping        the mapping used to match JSON properties/path with the ORM path.
	 * @param specifications the custom specifications.
	 */
	DynamicSpecification(final EntityManager em, final UiFilter filter, final Map<String, String> mapping,
			final Map<String, CustomSpecification> specifications) {
		this.em = em;
		this.filter = filter;
		this.mapping = MapUtils.emptyIfNull(mapping);
		this.specifications = MapUtils.emptyIfNull(specifications);
	}

	/**
	 * Return a custom predicate.
	 */
	private Predicate getCustomPredicate(final Root<U> root, final CriteriaBuilder cb, final BasicRule rule,
			final CriteriaQuery<?> query) {
		final var specification = specifications.get(rule.getField());
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
	private Predicate getGroupPredicate(final UiFilter group, final Root<U> root, final CriteriaQuery<?> query,
			final CriteriaBuilder cb) {
		// Build the predicates
		final var predicates = getPredicates(group, root, query, cb);

		// Build the specification
		if (predicates.isEmpty()) {
			return cb.conjunction();
		}
		final var filteredPredicates = predicates.toArray(new Predicate[0]);
		if (group.getGroupOp() == FilterOperator.AND) {
			return cb.and(filteredPredicates);
		}
		return cb.or(filteredPredicates);
	}

	/**
	 * Return the ORM path from the given rule.
	 */
	private <T> Path<T> getOrmPath(final Root<U> root, final BasicRule rule) {
		final var path = mapping.getOrDefault(rule.getField(), mapping.containsKey("*") ? rule.getField() : null);
		if (path == null) {
			// Invalid path, coding issue or SQL injection attempt
			log.error("Non mapped property '{}' found for entity class '{}'", rule.getField(),  root.getJavaType().getName());
			return null;
		}
		return getOrmPath(root, path);
	}

	/**
	 * Return the predicate corresponding to the given rule.
	 */
	private <X extends Comparable<Object>> Predicate getPredicate(final CriteriaBuilder cb, final BasicRule rule,
			final Expression<X> expression) {
		return PREDICATE_MAPPER.get(rule.getOp()).toPredicate(em, cb, rule.getData(), expression);
	}

	/**
	 * Return a predicate from a rule.
	 */
	private Predicate getPredicate(final Root<U> root, final CriteriaBuilder cb, final BasicRule rule,
			final CriteriaQuery<?> query) {
		if (rule.getOp() == RuleOperator.CT) {
			return getCustomPredicate(root, cb, rule, query);
		}
		final Expression<? extends Comparable<Object>> expression = getOrmPath(root, rule);
		if (expression == null) {
			// Non matched expression, ignore it...
			log.info("SQL injection attack ? Unable to map request rule for property {}", rule.getField());
			return null;
		}
		return getPredicate(cb, rule, expression);
	}

	/**
	 * Return the predicate corresponding the given rule.
	 */
	private Predicate getPredicate(final Root<U> root, final CriteriaQuery<?> query, final CriteriaBuilder cb,
			final UIRule rule) {
		if (rule instanceof BasicRule r) {
			return getPredicate(root, cb, r, query);
		}
		return getGroupPredicate((UiFilter) rule, root, query, cb);
	}

	/**
	 * Return the predicates list.
	 */
	private java.util.List<Predicate> getPredicates(final UiFilter group, final Root<U> root,
			final CriteriaQuery<?> query, final CriteriaBuilder cb) {
		return group.getRules().stream().map(rule -> getPredicate(root, query, cb, rule)).filter(Objects::nonNull).toList();
	}

	@Override
	public Predicate toPredicate(final Root<U> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
		return getGroupPredicate(filter, root, query, cb);
	}
}
