package org.ligoj.bootstrap.core.dao;

import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class of {@link PostgreSQL95NoSchemaDialect}
 */
public class PostgreSQL95NoSchemaDialectTest {

	@Test
	public void getNameQualifierSupport() {
		Assert.assertEquals(NameQualifierSupport.NONE, new PostgreSQL95NoSchemaDialect().getNameQualifierSupport());
	}

}
