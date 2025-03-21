/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.IdentifiableType;
import jodd.typeconverter.TypeConverterManager;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.model.domain.internal.BasicSqmPathSource;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.from.SqmJoin;
import org.hibernate.spi.NavigablePath;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

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
	 * @param root The {@link Root} used to resolve the path.
	 * @param path The path value. Nested path is accepted.
	 * @param <U>  The entity type referenced by the {@link Root}
	 * @param <T>  The resolved entity type of the path value.
	 * @return The resolved {@link Path} from the root.
	 */
	@SuppressWarnings("unchecked")
	protected <U, T> Path<T> getOrmPath(final Root<U> root, final String path) {
		var currentPath = (SqmPath<?>) root;
		for (final var pathFragment : path.split(DELIMITERS)) {
			currentPath = getNextPath(pathFragment, (From<?, ?>) currentPath);
		}

		// Fail-safe identifier access for non-singular target path
		if (currentPath instanceof SqmJoin<?, ?>) {
			final var idName = ((IdentifiableType<?>) ((Attribute<?, ?>) currentPath.getModel()).getDeclaringType()).getId(Object.class).getName();
			currentPath = getNextPath(idName, (From<?, ?>) currentPath);
		}
		return (Path<T>) currentPath;
	}

	@SuppressWarnings("unchecked")
	private <X> SqmPath<X> getNextPath(final String pathFragment, final From<?, ?> from) {
		var currentPath = (SqmPath<?>) from.get(pathFragment);
		fixAlias(from, aliasCounter);

		// Handle join. Does not manage many-to-many
		if (!(currentPath.getReferencedPathSource() instanceof BasicSqmPathSource<?>)) {
			currentPath = getPreviousJoinPath(from, currentPath.getNavigablePath().getLocalName());
			if (currentPath == null) {
				// if no join, we create it
				currentPath = fixAlias(from.join(pathFragment, JoinType.LEFT), aliasCounter);
			}
		}
		return (SqmPath<X>) currentPath;
	}

	/**
	 * Retrieve an existing join within the ones within the given root and that match to given attribute.
	 *
	 * @param from      the "from" source element.
	 * @param attribute the attribute to join
	 * @param <U>       The source type of the {@link Join}
	 * @param <T>       The resolved entity type of the path value.
	 * @return The join/fetch path if it exists.
	 */
	@SuppressWarnings("unchecked")
	protected <U, T> SqmPath<T> getPreviousJoinPath(final From<?, U> from, final String attribute) {

		// Search within current joins
		for (final var join : from.getJoins()) {
			if (join.getAttribute().getName().equals(attribute)) {
				return fixAlias((Selection<T>) join, aliasCounter);
			}
		}
		// Search within current fetch
		for (final var fetch : from.getFetches()) {
			if (fetch.getAttribute().getName().equals(attribute)) {
				return fixAlias((Selection<T>) fetch, aliasCounter);
			}
		}
		return null;
	}

	private <T> SqmPath<T> fixAlias(final Selection<T> join, final AtomicInteger integer) {
		if (join.getAlias() == null) {
			join.alias("_" + integer.incrementAndGet());
		}
		return (SqmPath<T>) join;
	}

	/**
	 * Return the raw data into the right type. Generic type is also handled.
	 *
	 * @param data       the data as String.
	 * @param expression the target expression.
	 * @param <Y>        The type of the {@link Expression}
	 * @return the data typed as much as possible to the target expression.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected static <Y> Y toRawData(final EntityManager em, final String data, final Expression<Y> expression) {

		// Guess the right compile time type, including generic type
		final var sf = (SessionFactoryImpl) em.unwrap(Session.class).getSessionFactory();
		final var path = ((JpaPath<?>) expression).getNavigablePath();
		final var metaModel = sf.getMetamodel();

		var parent = path;
		var reversePath = new ArrayList<NavigablePath>();
		do {
			reversePath.addFirst(parent);
			parent = parent.getParent();
		} while (parent != null);
		var model = metaModel.getEntityDescriptor(reversePath.getFirst().getLocalName()).getEntityMappingType();
		for (var i = 1; i < reversePath.size() - 1; i++) {
			final var join = reversePath.get(i).getLocalName();
			boolean found = false;
			for (var j = 0; j < model.getNumberOfAttributeMappings(); j++) {
				final var mapping = model.getAttributeMapping(j);
				if (join.equals(mapping.getFetchableName())) {
					if (mapping.isPluralAttributeMapping()) {
						model = mapping.asPluralAttributeMapping().getCollectionDescriptor().getOwnerEntityPersister();
						i++; // Skip next bag join
					} else {
						model = (EntityMappingType) mapping.getMappedType();
					}
					found = true;
					break;
				}
			}
			if (!found) {
				throw new ValidationJsonException("Invalid ORM path, cannot find '" + join + " in " + model);
			}
		}

		final var field = path.getLocalName();
		Class<?> expressionType = null;
		if (model.getIdentifierMapping().getAttributeName().equals(field)) {
			expressionType = model.getIdentifierMapping().getMappedType().getMappedJavaType().getJavaTypeClass();
		} else {
			for (var j = 0; j < model.getNumberOfAttributeMappings(); j++) {
				final var mapping = model.getAttributeMapping(j);
				if (field.equals(mapping.getFetchableName())) {
					expressionType = mapping.getMappedType().getMappedJavaType().getJavaTypeClass();
					break;
				}
			}
		}

		// Bind the data to the correct type
		final Object result;
		if (expressionType.isEnum()) {
			// Manage Enum type
			result = toEnum(data, (Expression<Enum>) expression);
		} else {
			// Involve bean utils to convert the data
			result = CONVERTER.convertType(data, expressionType);
		}

		return (Y) result;
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
