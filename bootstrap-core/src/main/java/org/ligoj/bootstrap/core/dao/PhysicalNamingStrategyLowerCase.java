package org.ligoj.bootstrap.core.dao;

import java.util.Locale;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Strategy forcing lower case for all data names.
 */
public class PhysicalNamingStrategyLowerCase extends org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl {
	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Identifier toPhysicalCatalogName(final Identifier name, final JdbcEnvironment context) {
		return toLowerCase(name);
	}

	@Override
	public Identifier toPhysicalSchemaName(final Identifier name, final JdbcEnvironment context) {
		return toLowerCase(name);
	}

	@Override
	public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment context) {
		return toLowerCase(name);
	}

	@Override
	public Identifier toPhysicalSequenceName(final Identifier name, final JdbcEnvironment context) {
		return toLowerCase(name);
	}

	@Override
	public Identifier toPhysicalColumnName(final Identifier name, final JdbcEnvironment context) {
		return toLowerCase(name);
	}

	/**
	 * Lower case the text, without touching the quotes.
	 */
	private Identifier toLowerCase(final Identifier name) {
		if (name == null) {
			return null;
		}

		// to lower case
		return new Identifier(name.getText().toLowerCase(Locale.ENGLISH), name.isQuoted());
	}
}
