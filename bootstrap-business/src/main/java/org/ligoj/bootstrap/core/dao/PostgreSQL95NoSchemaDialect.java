package org.ligoj.bootstrap.core.dao;

import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

/**
 * PostgreSQL dialect with disabled schema.
 */
public class PostgreSQL95NoSchemaDialect extends PostgreSQL95Dialect {

	@Override
	public NameQualifierSupport getNameQualifierSupport() {
		return NameQualifierSupport.NONE;
	}

}
