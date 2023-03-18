/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

/**
 * PostgreSQL dialect with disabled schema.
 */
public class PostgreSQL95NoSchemaDialect extends PostgreSQLDialect {

	@Override
	public NameQualifierSupport getNameQualifierSupport() {
		return NameQualifierSupport.NONE;
	}

}
