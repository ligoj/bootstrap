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
		var contrib = newFunctionContributions();
		new MySQL5InnoDBUtf8Dialect().initializeFunctionRegistry(contrib);
		Mockito.verify(basicTypeRegistry, Mockito.atLeastOnce()).resolve(StandardBasicTypes.DOUBLE);
	}
}
