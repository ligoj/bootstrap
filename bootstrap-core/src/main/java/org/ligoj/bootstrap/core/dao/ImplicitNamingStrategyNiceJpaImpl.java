/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.boot.model.naming.*;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;

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
		return toIdentifier(generateName("FK_", new Table(null, source.getTableName().getText()),
				toColumns(source.getColumnNames())), source.getBuildingContext());
	}

	private static String generateName(String prefix, Table table, List<Column> columns) {
		// Use a concatenation that guarantees uniqueness, even if identical names
		// exist between all table and column identifiers.
		final StringBuilder sb = new StringBuilder("table`" + table.getName() + "`");
		// Ensure a consistent ordering of columns, regardless of the order they were bound.
		// Clone the list, as sometimes a set of order-dependent Column bindings are given.
		columns.stream()
				.filter((Object thing) -> thing instanceof Column)
				.sorted(Comparator.comparing(Column::getName))
				.forEach(column -> sb.append("column`").append(column.getName()).append("`"));
		return prefix + hashedName(sb.toString(), "MD5");
	}

	static String hashedName(String name, String algorithm) {
		try {
			final MessageDigest md = MessageDigest.getInstance(algorithm);
			md.reset();
			md.update(name.getBytes());
			// By converting to base 35 (full alphanumeric), we guarantee
			// that the length of the name will always be smaller than the 30
			// character identifier restriction enforced by a few dialects.
			return new BigInteger(1, md.digest()).toString(35);
		} catch (NoSuchAlgorithmException e) {
			throw new HibernateException("Unable to generate a hashed Constraint name", e);
		}
	}

	/**
	 * Return the column of given identifiers.
	 *
	 * @param identifiers The identifiers to map.
	 * @return The column of given identifiers.
	 */
	protected List<Column> toColumns(final List<Identifier> identifiers) {
		return identifiers.stream().map(column -> new Column(column.getText())).toList();
	}

	@Override
	public Identifier determineUniqueKeyName(final ImplicitUniqueKeyNameSource source) {
		return toIdentifier(generateName("UK_", new Table(source.getTableName().getText()),
				toColumns(source.getColumnNames())), source.getBuildingContext());
	}
}
