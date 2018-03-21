/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link PostgreSQL95NoSchemaDialect}
 */
public class PostgreSQL95NoSchemaDialectTest {

	@Test
	public void getNameQualifierSupport() {
		Assertions.assertEquals(NameQualifierSupport.NONE, new PostgreSQL95NoSchemaDialect().getNameQualifierSupport());
	}

}
