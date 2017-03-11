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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test class of {@link ImplicitNamingStrategyNiceJpaImpl}
 */
public class ImplicitNamingStrategyNiceJpaImplTest {

	@Test
	public void determineUniqueKeyName() {
		final ImplicitUniqueKeyNameSource source = Mockito.mock(ImplicitUniqueKeyNameSource.class);
		mockContext(source);
		Mockito.when(source.getTableName()).thenReturn(new DatabaseIdentifier("MyTa_ble"));
		final List<Identifier> columnsIdentifier = new ArrayList<>();
		columnsIdentifier.add(new DatabaseIdentifier("MyCol_umn1"));
		columnsIdentifier.add(new DatabaseIdentifier("MyCol_umn2"));
		Mockito.when(source.getColumnNames()).thenReturn(columnsIdentifier);
		final Identifier identifier = new ImplicitNamingStrategyNiceJpaImpl().determineUniqueKeyName(source);

		Assert.assertEquals("UK_bdj7f5p3skrieson5es1km8t9", identifier.getText());
	}

	@Test
	public void determineAnyKeyColumnName() {
		final ImplicitAnyKeyColumnNameSource source = Mockito.mock(ImplicitAnyKeyColumnNameSource.class);
		mockContext(source);
		final AttributePath attributePath= Mockito.mock(AttributePath.class);
		Mockito.when(attributePath.getProperty()).thenReturn("myProperty");
		Mockito.when(source.getAttributePath()).thenReturn(attributePath);
		final Identifier identifier = new ImplicitNamingStrategyNiceJpaImpl().determineAnyKeyColumnName(source);
		Assert.assertEquals("my_property", identifier.getText());
	}

	private void mockContext(final ImplicitNameSource source) {
		final MetadataBuildingContext context = Mockito.mock(MetadataBuildingContext.class);
		final InFlightMetadataCollector collector = Mockito.mock(InFlightMetadataCollector.class);
		final Database database = Mockito.mock(Database.class);
		final JdbcEnvironment jdbcEnvironment = Mockito.mock(JdbcEnvironment.class);
		final IdentifierHelper identifierHelper = Mockito.mock(IdentifierHelper.class);
		Mockito.when(identifierHelper.toIdentifier(ArgumentMatchers.anyString())).then(new Answer<Identifier>() {

			@Override
			public Identifier answer(final InvocationOnMock invocation) {
				return new DatabaseIdentifier((String)invocation.getArguments()[0]) ;
			}
		});
		Mockito.when(jdbcEnvironment.getIdentifierHelper()).thenReturn(identifierHelper);
		Mockito.when(database.getJdbcEnvironment()).thenReturn(jdbcEnvironment);
		Mockito.when(collector.getDatabase()).thenReturn(database);
		Mockito.when(context.getMetadataCollector()).thenReturn(collector);
		Mockito.when(source.getBuildingContext()).thenReturn(context);
	}

	
	@Test
	public void determineJoinColumnNameCollection() {
		final ImplicitJoinColumnNameSource source = Mockito.mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		Mockito.when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION);
		Mockito.when(source.getReferencedTableName()).thenReturn(new DatabaseIdentifier("MyTa_ble"));
		final Identifier identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assert.assertEquals("MyTa_ble", identifier.getText());
	}
	
	@Test
	public void determineJoinColumnNameNoAttribute() {
		final ImplicitJoinColumnNameSource source = Mockito.mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		Mockito.when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ENTITY);
		Mockito.when(source.getReferencedTableName()).thenReturn(new DatabaseIdentifier("MyTa_ble"));
		final Identifier identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assert.assertEquals("MyTa_ble", identifier.getText());
	}
	
	@Test
	public void determineJoinColumnName() {
		final ImplicitJoinColumnNameSource source = Mockito.mock(ImplicitJoinColumnNameSource.class);
		mockContext(source);
		Mockito.when(source.getNature()).thenReturn(ImplicitJoinColumnNameSource.Nature.ENTITY);
		final AttributePath attributePath= Mockito.mock(AttributePath.class);
		Mockito.when(attributePath.getProperty()).thenReturn("myProperty");
		Mockito.when(source.getAttributePath()).thenReturn(attributePath);
		Mockito.when(source.getReferencedTableName()).thenReturn(new DatabaseIdentifier("MyTa_ble"));
		final Identifier identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinColumnName(source);
		Assert.assertEquals("my_property", identifier.getText());
	}
	
	@Test
	public void determineJoinTableName() {
		final ImplicitJoinTableNameSource source = Mockito.mock(ImplicitJoinTableNameSource.class);
		mockContext(source);
		final AttributePath attributePath= Mockito.mock(AttributePath.class);
		Mockito.when(attributePath.getProperty()).thenReturn("myProperty");
		Mockito.when(source.getAssociationOwningAttributePath()).thenReturn(attributePath);
		Mockito.when(source.getOwningPhysicalTableName()).thenReturn("Table1");
		final Identifier identifier = new ImplicitNamingStrategyNiceJpaImpl().determineJoinTableName(source);
		Assert.assertEquals("Table1_my_property", identifier.getText());
	}
	@Test
	public void determineForeignKeyName() {
		final ImplicitForeignKeyNameSource source = Mockito.mock(ImplicitForeignKeyNameSource.class);
		mockContext(source);
		Mockito.when(source.getTableName()).thenReturn(new DatabaseIdentifier("MyTa_ble"));
		final List<Identifier> columnsIdentifier = new ArrayList<>();
		columnsIdentifier.add(new DatabaseIdentifier("MyCol_umn1"));
		columnsIdentifier.add(new DatabaseIdentifier("MyCol_umn2"));
		Mockito.when(source.getColumnNames()).thenReturn(columnsIdentifier);
		final Identifier identifier = new ImplicitNamingStrategyNiceJpaImpl().determineForeignKeyName(source);
		Assert.assertEquals("FK_bdj7f5p3skrieson5es1km8t9", identifier.getText());
	}
	
	
}
