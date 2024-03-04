/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.hibernate.type.StandardBasicTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link PostgreSQL95NoSchemaDialect}
 */
class PostgreSQL95NoSchemaDialectTest extends AbstractDialectTest{

	@Test
	void getNameQualifierSupport() {
		Assertions.assertEquals(NameQualifierSupport.NONE, new PostgreSQL95NoSchemaDialect().getNameQualifierSupport());
	}

	@Test
	void initializeFunctionRegistry() {
		new PostgreSQL95NoSchemaDialect().initializeFunctionRegistry(newFunctionContributions());
		Mockito.verify(basicTypeRegistry, Mockito.atLeastOnce()).resolve(StandardBasicTypes.DOUBLE);
	}
}
