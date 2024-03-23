/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.type.StandardBasicTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test of {@link HSQLDialect25}
 */
class HSQLDialect25Test extends AbstractDialectTest {

	@Test
	void constructor() {
		Assertions.assertTrue(new HSQLDialect25().getKeywords().contains("period"));
	}

	@Test
	void initializeFunctionRegistry() {
		var contrib = newFunctionContributions();
		new HSQLDialect25().initializeFunctionRegistry(contrib);
		Mockito.verify(basicTypeRegistry, Mockito.atLeastOnce()).resolve(StandardBasicTypes.DOUBLE);
	}
}
