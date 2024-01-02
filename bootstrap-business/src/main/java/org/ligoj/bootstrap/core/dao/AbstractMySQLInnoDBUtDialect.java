/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.hibernate.type.StandardBasicTypes;

import static org.hibernate.query.sqm.produce.function.FunctionParameterType.NUMERIC;

/**
 * "MySql" dialect with enforced UTF-8 encoding.
 */
abstract class AbstractMySQLInnoDBUtDialect extends MySQLDialect {

	protected AbstractMySQLInnoDBUtDialect(DatabaseVersion version) {
		super(version);
		registerKeyword("USAGE");
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
		var functionRegistry = functionContributions.getFunctionRegistry();
		var typeConfiguration = functionContributions.getTypeConfiguration();
		var basicTypeRegistry = typeConfiguration.getBasicTypeRegistry();
		var doubleType = basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE);
		functionRegistry.namedDescriptorBuilder("ceil")
				.setExactArgumentCount(1)
				.setParameterTypes(NUMERIC)
				.setInvariantType(doubleType)
				.register();
	}
}
