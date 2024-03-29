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
 * Test class of {@link MySQL8InnoDBUtf8Dialect}
 */
class MySQL8InnoDBUtf8DialectTest  extends AbstractDialectTest{

	@Test
	void getTableTypeString() {
		Assertions.assertEquals(" engine=InnoDB DEFAULT CHARSET=utf8",
				new MySQL8InnoDBUtf8Dialect().getTableTypeString());
	}

	@Test
	void getNameQualifierSupport() {
		Assertions.assertEquals(NameQualifierSupport.NONE, new MySQL8InnoDBUtf8Dialect().getNameQualifierSupport());
	}
	@Test
	void initializeFunctionRegistry() {
		new MySQL8InnoDBUtf8Dialect().initializeFunctionRegistry(newFunctionContributions());
		Mockito.verify(basicTypeRegistry, Mockito.atLeastOnce()).resolve(StandardBasicTypes.DOUBLE);
	}
}
