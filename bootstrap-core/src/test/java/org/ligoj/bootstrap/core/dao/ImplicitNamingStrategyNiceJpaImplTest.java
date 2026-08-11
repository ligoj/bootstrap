/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.HibernateException;
import org.hibernate.boot.model.naming.*;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class of {@link ImplicitNamingStrategyNiceJpaImpl}
 */
class ImplicitNamingStrategyNiceJpaImplTest {

	@Test
    void determineUniqueKeyName() {
		final var source = mock(ImplicitUniqueKeyNameSource.class);
		mockContext(source);
		when(source.getTableName()).thenReturn(DatabaseIdentifier.toIdentifier("null"));
		final List<Identifier> columnsIdentifier = new ArrayList<>();
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn1"));
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn2"));
		when(source.getColumnNames()).thenReturn(columnsIdentifier);

		var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineUniqueKeyName(source);
		Assertions.assertEquals("UK_bg6a6jkepii31sno6kq8jv1g5", identifier.getText());

		when(source.getTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		identifier = new ImplicitNamingStrategyNiceJpaImpl().determineUniqueKeyName(source);
		Assertions.assertEquals("UK_bdj7f5p3skrieson5es1km8t9", identifier.getText());

	}

	@Test
	void hashedName() {
		Assertions.assertEquals("b092g87witldyyieg524ayqqn", ImplicitNamingStrategyNiceJpaImpl.hashedName("sample", "MD5"));
	}
	@Test
	void hashedNameKo() {
		Assertions.assertThrows(HibernateException.class, ()-> ImplicitNamingStrategyNiceJpaImpl.hashedName("any", "__"));
	}
	@Test
    void determineAnyKeyColumnName() {
		final var source = mock(ImplicitAnyKeyColumnNameSource.class);
		mockContext(source);
		final var attributePath= mock(AttributePath.class);
		when(attributePath.getProperty()).thenReturn("myProperty");
		when(source.getAttributePath()).thenReturn(attributePath);
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineAnyKeyColumnName(source);
		Assertions.assertEquals("my_property", identifier.getText());
	}

	private void mockContext(final ImplicitNameSource source) {
		final var context = mock(MetadataBuildingContext.class);
		final var collector = mock(InFlightMetadataCollector.class);
		final var database = mock(Database.class);
		final var jdbcEnvironment = mock(JdbcEnvironment.class);
		final var identifierHelper = mock(IdentifierHelper.class);
		when(identifierHelper.toIdentifier(ArgumentMatchers.anyString())).then((Answer<Identifier>) invocation -> DatabaseIdentifier.toIdentifier((String)invocation.getArguments()[0]));
		when(jdbcEnvironment.getIdentifierHelper()).thenReturn(identifierHelper);
		when(database.getJdbcEnvironment()).thenReturn(jdbcEnvironment);
		when(collector.getDatabase()).thenReturn(database);
		when(context.getMetadataCollector()).thenReturn(collector);
		when(source.getBuildingContext()).thenReturn(context);
	}

	
	@Test
    void determineJoinColumnNameCollection() {
		final var source = mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION);
		when(source.getReferencedTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assertions.assertEquals("MyTa_ble", identifier.getText());
	}
	
	@Test
    void determineJoinColumnNameNoAttribute() {
		final var source = mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ENTITY);
		when(source.getReferencedTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assertions.assertEquals("MyTa_ble", identifier.getText());
	}
	
	@Test
    void determineJoinColumnName() {
		final var source = mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ENTITY);
		final var attributePath= mock(AttributePath.class);
		when(attributePath.getProperty()).thenReturn("myProperty");
		when(source.getAttributePath()).thenReturn(attributePath);
		when(source.getReferencedTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assertions.assertEquals("my_property", identifier.getText());
	}
	
	@Test
    void determineJoinTableName() {
		final var source = mock(ImplicitJoinTableNameSource.class);
		mockContext(source);
		final var attributePath= mock(AttributePath.class);
		when(attributePath.getProperty()).thenReturn("myProperty");
		when(source.getAssociationOwningAttributePath()).thenReturn(attributePath);
		when(source.getOwningPhysicalTableName()).thenReturn("Table1");
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinTableName(source);
		Assertions.assertEquals("Table1_my_property", identifier.getText());
	}
	@Test
    void determineForeignKeyName() {
		final var source = mock(ImplicitForeignKeyNameSource.class);
		mockContext(source);
		when(source.getTableName()).thenReturn(DatabaseIdentifier.toIdentifier("MyTa_ble"));
		final List<Identifier> columnsIdentifier = new ArrayList<>();
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn1"));
		columnsIdentifier.add(DatabaseIdentifier.toIdentifier("MyCol_umn2"));
		when(source.getColumnNames()).thenReturn(columnsIdentifier);
		final var identifier = new ImplicitNamingStrategyNiceJpaImpl().determineForeignKeyName(source);
		Assertions.assertEquals("FK_bdj7f5p3skrieson5es1km8t9", identifier.getText());
	}
	
	
}
