/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

/**
 * "PostgreSql" dialect with disabled schema.
 */
public class PostgreSQL95NoSchemaDialect extends PostgreSQLDialect implements RegistrableDialect {

	/**
	 * Default constructor with keyword registration.
	 */
	public PostgreSQL95NoSchemaDialect() {
		super();
		registerKeyword("LIMIT");
		registerKeyword("USAGE");
		registerKeyword("MIN");
		registerKeyword("MAX");
	}

	@Override
	public NameQualifierSupport getNameQualifierSupport() {
		return NameQualifierSupport.NONE;
	}

	@Override
	public void initializeFunctionRegistry(FunctionContributions functionContributions) {
		super.initializeFunctionRegistry(functionContributions);
		registerFunctions(functionContributions);
	}
}
