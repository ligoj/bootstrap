package org.ligoj.bootstrap.core.dao;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test non spring integration behaviors of {@link PhysicalNamingStrategyLowerCase} class.
 */
public class PhysicalNamingStrategyLowerCaseTest {

	private static final String ANY = "any";
	private static final Identifier IDENTIFIER = new Identifier("ANY", false);
	private PhysicalNamingStrategyLowerCase strategy;

	/**
	 * Dummy class only there to throw an {@link IllegalAccessException} for coverage test.
	 */
	@Before
	public void prepare() {
		strategy = new PhysicalNamingStrategyLowerCase();
	}

	@Test
	public void toPhysicalCatalogName() {
		Assert.assertEquals(ANY, strategy.toPhysicalCatalogName(IDENTIFIER, null).getText());
	}

	@Test
	public void toPhysicalColumnName() {
		Assert.assertEquals(ANY, strategy.toPhysicalColumnName(IDENTIFIER, null).getText());
	}

	@Test
	public void toPhysicalSchemaName() {
		Assert.assertEquals(ANY, strategy.toPhysicalSchemaName(IDENTIFIER, null).getText());
	}

	@Test
	public void toPhysicalSequenceName() {
		Assert.assertEquals(ANY, strategy.toPhysicalSequenceName(IDENTIFIER, null).getText());
	}

	@Test
	public void toPhysicalTableName() {
		Assert.assertEquals(ANY, strategy.toPhysicalTableName(IDENTIFIER, null).getText());
	}

	@Test
	public void toPhysicalTableNameNull() {
		Assert.assertNull(strategy.toPhysicalTableName(null, null));
	}

}
