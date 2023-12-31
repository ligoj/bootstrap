/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.hibernate.type.StandardBasicTypes;

import static org.hibernate.query.sqm.produce.function.FunctionParameterType.NUMERIC;

/**
 * "PostgreSql" dialect with disabled schema.
 */
public class PostgreSQL95NoSchemaDialect extends PostgreSQLDialect {

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
