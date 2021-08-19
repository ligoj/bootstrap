/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitAnyKeyColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.Table;

/**
 * Implements the original legacy naming behavior :
 * <ul>
 * <li>no "_id" for join column</li>
 * <li>FK has "FK_" as prefix</li>
 * <li>Join column use table name instead of entity name</li>
 * </ul>
 */
public class ImplicitNamingStrategyNiceJpaImpl
		extends org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Identifier determineAnyKeyColumnName(final ImplicitAnyKeyColumnNameSource source) {
		return toIdentifier(transformAttributePath(source.getAttributePath()), source.getBuildingContext());
	}

	@Override
	public Identifier determineJoinColumnName(final ImplicitJoinColumnNameSource source) {
		if (source.getNature() == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION
				|| source.getAttributePath() == null) {
			return source.getReferencedTableName();
		}
		return toIdentifier(transformAttributePath(source.getAttributePath()), source.getBuildingContext());
	}

	@Override
	public Identifier determineJoinTableName(final ImplicitJoinTableNameSource source) {
		final var name = source.getOwningPhysicalTableName() + '_'
				+ transformAttributePath(source.getAssociationOwningAttributePath());
		return toIdentifier(name, source.getBuildingContext());
	}

	/**
	 * For JPA standards we typically need the unqualified name. However, a more usable impl tends to use the whole
	 * path. This method provides an easy hook for subclasses to accomplish that
	 *
	 * @param attributePath The attribute path
	 * @return The extracted name
	 */
	@Override
	protected String transformAttributePath(final AttributePath attributePath) {
		return StringUtils
				.lowerCase(String.join("_", StringUtils.splitByCharacterTypeCamelCase(attributePath.getProperty())));
	}

	@Override
	public Identifier determineForeignKeyName(final ImplicitForeignKeyNameSource source) {
		return toIdentifier(Constraint.generateName("FK_", new Table(source.getTableName().getText()),
				toColumns(source.getColumnNames())), source.getBuildingContext());
	}

	/**
	 * Return the column of given identifiers.
	 * 
	 * @param identifiers The identifiers to map.
	 * @return The column of given identifiers.
	 */
	protected List<Column> toColumns(final List<Identifier> identifiers) {
		return identifiers.stream().map(column -> new Column(column.getText())).collect(Collectors.toList());
	}

	@Override
	public Identifier determineUniqueKeyName(final ImplicitUniqueKeyNameSource source) {
		return toIdentifier(Constraint.generateName("UK_", new Table(source.getTableName().getText()),
				toColumns(source.getColumnNames())), source.getBuildingContext());
	}
}
