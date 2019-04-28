/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.IdentifiableType;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.criteria.internal.PathImplementor;
import org.hibernate.query.criteria.internal.path.SingularAttributeJoin;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;

import com.googlecode.gentyref.GenericTypeReflector;

import jodd.typeconverter.TypeConverterManager;

/**
 * Common JPA tools to manipulate specification path.
 */
public abstract class AbstractSpecification {

	/**
	 * String source converter.
	 */
	private static final TypeConverterManager CONVERTER = TypeConverterManager.get();

	/**
	 * Spring data delimiters expressions.
	 */
	public static final String DELIMITERS = "[_.]";

	private final AtomicInteger aliasCounter = new AtomicInteger();

	/**
	 * Return the ORM path from the given rule.
	 *
	 * @param root
	 *            The {@link Root} used to resolved the path.
	 * @param path
	 *            The path value. Nested path is accepted.
	 * @param <U>
	 *            The entity type referenced by the {@link Root}
	 * @param <T>
	 *            The resolved entity type of the path value.
	 * @return The resolved {@link Path} from the root.
	 */
	@SuppressWarnings("unchecked")
	protected <U, T> Path<T> getOrmPath(final Root<U> root, final String path) {
        var currentPath = (PathImplementor<?>) root;
		for (final var pathFragment : path.split(DELIMITERS)) {
			currentPath = getNextPath(pathFragment, (From<?, ?>) currentPath);
		}

		// Fail safe identifier access for non singular target path
		if (currentPath instanceof SingularAttributeJoin) {
			currentPath = getNextPath(((IdentifiableType<?>) currentPath.getModel()).getId(Object.class).getName(),
					(From<?, ?>) currentPath);
		}
		return (Path<T>) currentPath;
	}

	@SuppressWarnings("unchecked")
	private <X> PathImplementor<X> getNextPath(final String pathFragment, final From<?, ?> from) {
        var currentPath = (PathImplementor<?>) from.get(pathFragment);
		fixAlias(from, aliasCounter);

		// Handle join. Does not manage many-to-many
		if (currentPath.getAttribute().getPersistentAttributeType() != PersistentAttributeType.BASIC) {
			currentPath = getJoinPath(from, currentPath.getAttribute());
			if (currentPath == null) {
				// if no join, we create it
				currentPath = fixAlias(from.join(pathFragment, JoinType.LEFT), aliasCounter);
			}
		}
		return (PathImplementor<X>) currentPath;
	}

	/**
	 * Retrieve an existing join within the ones within the given root and that match to given attribute.
	 *
	 * @param from
	 *            the from source element.
	 * @param attribute
	 *            the attribute to join
	 * @return The join/fetch path if it exists.
	 * @param <U>
	 *            The source type of the {@link Join}
	 * @param <T>
	 *            The resolved entity type of the path value.
	 */
	@SuppressWarnings("unchecked")
	protected <U, T> PathImplementor<T> getJoinPath(final From<?, U> from, final Attribute<?, ?> attribute) {

		// Search within current joins
		for (final var join : from.getJoins()) {
			if (join.getAttribute().equals(attribute)) {
				return fixAlias((Join<U, T>) join, aliasCounter);
			}
		}
		return null;
	}

	private <T> PathImplementor<T> fixAlias(final Selection<T> join, final AtomicInteger integer) {
		if (join.getAlias() == null) {
			join.alias("_" + integer.incrementAndGet());
		}
		return (PathImplementor<T>) join;
	}

	/**
	 * Return the raw data into the right type. Generic type is also handled.
	 *
	 * @param data
	 *            the data as String.
	 * @param expression
	 *            the target expression.
	 * @return the data typed as much as possible to the target expression.
	 * @param <Y>
	 *            The type of the {@link Expression}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static <Y> Y toRawData(final String data, final Expression<Y> expression) {

		// Guess the right compile time type, including generic type
		final var field = (Field) ((SingularAttributePath<?>) expression).getAttribute().getJavaMember();
		final Class<?> entity = ((SingularAttributePath<?>) expression).getPathSource().getJavaType();
		final var expressionType = (Class<?>) GenericTypeReflector.getExactFieldType(field, entity);

		// Bind the data to the correct type
		final Y result;
		if (expressionType == Date.class) {
			// For Date, only milliseconds are managed
			result = (Y) new Date(Long.parseLong(data));
		} else if (expressionType.isEnum()) {
			// Manage Enum type
			result = (Y) toEnum(data, (Expression<Enum>) expression);
		} else {
			// Involve bean utils to convert the data
			result = (Y) CONVERTER.convertType(data, expressionType);
		}

		return result;
	}

	/**
	 * Get {@link Enum} value from the string raw data. Accept lower and upper case for the match.
	 */
	@SuppressWarnings("unchecked")
	private static <Y extends Enum<Y>> Enum<Y> toEnum(final String data, final Expression<Y> expression) {
		if (StringUtils.isNumeric(data)) {
			// Get Enum value by its ordinal
			return expression.getJavaType().getEnumConstants()[Integer.parseInt(data)];
		}

		// Get Enum value by its exact name
		Enum<Y> fromName = EnumUtils.getEnum((Class<Y>) expression.getJavaType(), data);

		// Get Enum value by its upper case name
		if (fromName == null) {
			fromName = Enum.valueOf((Class<Y>) expression.getJavaType(), data.toUpperCase(Locale.ENGLISH));
		}
		return fromName;
	}

}
