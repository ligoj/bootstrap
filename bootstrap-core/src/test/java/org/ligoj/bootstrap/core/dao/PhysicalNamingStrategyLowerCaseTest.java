/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test non-spring integration behaviors of {@link PhysicalNamingStrategyLowerCase} class.
 */
class PhysicalNamingStrategyLowerCaseTest {

	private static final String ANY = "any";
	private static final Identifier IDENTIFIER = new Identifier("ANY", false);
	private PhysicalNamingStrategyLowerCase strategy;

	/**
	 * Dummy class only there to throw an {@link IllegalAccessException} for coverage test.
	 */
	@BeforeEach
    void prepare() {
		strategy = new PhysicalNamingStrategyLowerCase();
	}

	@Test
    void toPhysicalCatalogName() {
		Assertions.assertEquals(ANY, strategy.toPhysicalCatalogName(IDENTIFIER, null).getText());
	}

	@Test
    void toPhysicalColumnName() {
		Assertions.assertEquals(ANY, strategy.toPhysicalColumnName(IDENTIFIER, null).getText());
	}

	@Test
    void toPhysicalSchemaName() {
		Assertions.assertEquals(ANY, strategy.toPhysicalSchemaName(IDENTIFIER, null).getText());
	}

	@Test
    void toPhysicalSequenceName() {
		Assertions.assertEquals(ANY, strategy.toPhysicalSequenceName(IDENTIFIER, null).getText());
	}

	@Test
    void toPhysicalTableName() {
		Assertions.assertEquals(ANY, strategy.toPhysicalTableName(IDENTIFIER, null).getText());
	}

	@Test
    void toPhysicalTableNameNull() {
		Assertions.assertNull(strategy.toPhysicalTableName(null, null));
	}

}
