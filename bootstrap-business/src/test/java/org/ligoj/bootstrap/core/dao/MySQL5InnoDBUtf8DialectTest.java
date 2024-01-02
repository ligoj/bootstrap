/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link MySQL5InnoDBUtf8Dialect}
 */
class MySQL5InnoDBUtf8DialectTest extends AbstractDialectTest {

	@Test
	void getTableTypeString() {
		Assertions.assertEquals(" engine=InnoDB DEFAULT CHARSET=utf8", new MySQL5InnoDBUtf8Dialect().getTableTypeString());
	}

	@Test
	void getNameQualifierSupport() {
		Assertions.assertEquals(NameQualifierSupport.NONE, new MySQL5InnoDBUtf8Dialect().getNameQualifierSupport());
	}

	@Test
	void initializeFunctionRegistry() {
		new MySQL5InnoDBUtf8Dialect().initializeFunctionRegistry(newFunctionContributions());
	}
}
