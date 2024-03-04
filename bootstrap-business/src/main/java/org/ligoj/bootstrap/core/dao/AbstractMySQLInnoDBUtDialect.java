/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

/**
 * "MySql" dialect with enforced UTF-8 encoding.
 */
abstract class AbstractMySQLInnoDBUtDialect extends MySQLDialect implements RegistrableDialect {

	protected AbstractMySQLInnoDBUtDialect(DatabaseVersion version) {
		super(version);
		registerKeyword("USAGE");
		registerKeyword("LIMIT");
	}

	@Override
	public String getTableTypeString() {
		return super.getTableTypeString() + " DEFAULT CHARSET=utf8";
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
