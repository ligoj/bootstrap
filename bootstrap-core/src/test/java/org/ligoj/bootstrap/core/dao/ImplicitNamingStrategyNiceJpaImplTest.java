/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.model.naming.DatabaseIdentifier;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitAnyKeyColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource;
import org.hibernate.boot.model.naming.ImplicitNameSource;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Test class of {@link ImplicitNamingStrategyNiceJpaImpl}
 */
class ImplicitNamingStrategyNiceJpaImplTest {

	@Test
    void determineUniqueKeyName() {
		final var source = Mockito.mock(ImplicitUniqueKeyNameSource.class);
		mockContext(source);
		Mockito.when(source.getTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final List<Identifier> columnsIdentifier = new ArrayList<>();
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn1"));
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn2"));
		Mockito.when(source.getColumnNames()).thenReturn(columnsIdentifier);
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineUniqueKeyName(source);

		Assertions.assertEquals("UK_bg6a6jkepii31sno6kq8jv1g5", identifier.getText());
	}

	@Test
    void determineAnyKeyColumnName() {
		final var source = Mockito.mock(ImplicitAnyKeyColumnNameSource.class);
		mockContext(source);
		final var attributePath= Mockito.mock(AttributePath.class);
		Mockito.when(attributePath.getProperty()).thenReturn("myProperty");
		Mockito.when(source.getAttributePath()).thenReturn(attributePath);
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineAnyKeyColumnName(source);
		Assertions.assertEquals("my_property", identifier.getText());
	}

	private void mockContext(final ImplicitNameSource source) {
		final var context = Mockito.mock(MetadataBuildingContext.class);
		final var collector = Mockito.mock(InFlightMetadataCollector.class);
		final var database = Mockito.mock(Database.class);
		final var jdbcEnvironment = Mockito.mock(JdbcEnvironment.class);
		final var identifierHelper = Mockito.mock(IdentifierHelper.class);
		Mockito.when(identifierHelper.toIdentifier(ArgumentMatchers.anyString())).then((Answer<Identifier>) invocation -> DatabaseIdentifier.toIdentifier((String)invocation.getArguments()[0]));
		Mockito.when(jdbcEnvironment.getIdentifierHelper()).thenReturn(identifierHelper);
		Mockito.when(database.getJdbcEnvironment()).thenReturn(jdbcEnvironment);
		Mockito.when(collector.getDatabase()).thenReturn(database);
		Mockito.when(context.getMetadataCollector()).thenReturn(collector);
		Mockito.when(source.getBuildingContext()).thenReturn(context);
	}

	
	@Test
    void determineJoinColumnNameCollection() {
		final var source = Mockito.mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		Mockito.when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION);
		Mockito.when(source.getReferencedTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assertions.assertEquals("MyTa_ble", identifier.getText());
	}
	
	@Test
    void determineJoinColumnNameNoAttribute() {
		final var source = Mockito.mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		Mockito.when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ENTITY);
		Mockito.when(source.getReferencedTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assertions.assertEquals("MyTa_ble", identifier.getText());
	}
	
	@Test
    void determineJoinColumnName() {
		final var source = Mockito.mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		Mockito.when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ENTITY);
		final var attributePath= Mockito.mock(AttributePath.class);
		Mockito.when(attributePath.getProperty()).thenReturn("myProperty");
		Mockito.when(source.getAttributePath()).thenReturn(attributePath);
		Mockito.when(source.getReferencedTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assertions.assertEquals("my_property", identifier.getText());
	}
	
	@Test
    void determineJoinTableName() {
		final var source = Mockito.mock(ImplicitJoinTableNameSource.class);
		mockContext(source);
		final var attributePath= Mockito.mock(AttributePath.class);
		Mockito.when(attributePath.getProperty()).thenReturn("myProperty");
		Mockito.when(source.getAssociationOwningAttributePath()).thenReturn(attributePath);
		Mockito.when(source.getOwningPhysicalTableName()).thenReturn("Table1");
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinTableName(source);
		Assertions.assertEquals("Table1_my_property", identifier.getText());
	}
	@Test
    void determineForeignKeyName() {
		final var source = Mockito.mock(ImplicitForeignKeyNameSource.class);
		mockContext(source);
		Mockito.when(source.getTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final List<Identifier> columnsIdentifier = new ArrayList<>();
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn1"));
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn2"));
		Mockito.when(source.getColumnNames()).thenReturn(columnsIdentifier);
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineForeignKeyName(source);
		Assertions.assertEquals("FK_bdj7f5p3skrieson5es1km8t9", identifier.getText());
	}
	
	
}
