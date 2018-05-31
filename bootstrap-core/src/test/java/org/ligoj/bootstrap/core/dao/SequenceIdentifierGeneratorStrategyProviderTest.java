/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.Properties;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ObjectNameNormalizer;
import org.hibernate.boot.model.relational.QualifiedName;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MySQL55Dialect;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.enhanced.DatabaseStructure;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.dao.SequenceIdentifierGeneratorStrategyProvider.OptimizedSequenceStyleGenerator;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test of {@link SequenceIdentifierGeneratorStrategyProvider} implementation.
 */
public class SequenceIdentifierGeneratorStrategyProviderTest {
	/**
	 * Check strategy configuration. @throws SecurityException @throws NoSuchMethodException @throws
	 * InvocationTargetException @throws
	 */
	@Test
	public void testFactoryConfiguration() throws ReflectiveOperationException {
		Assertions.assertEquals(OptimizedSequenceStyleGenerator.class, SequenceIdentifierGeneratorStrategyProvider.class
				.getDeclaredConstructor().newInstance().getStrategies().get(SequenceStyleGenerator.class.getName()));
	}

	/**
	 * Check strategy configuration.
	 */
	@Test
	public void testConfiguration() {
		final Properties params = new Properties();
		params.put("identity_tables", "summy.seg");
		params.put(PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER, new ObjectNameNormalizer() {

			@Override
			protected MetadataBuildingContext getBuildingContext() {
				return null;
			}
		});

		OptimizedSequenceStyleGenerator optimizedSequenceStyleGenerator = newStyleGenerator();
		optimizedSequenceStyleGenerator.configure(StringType.INSTANCE, params, newServiceRegistry());
	}

	private OptimizedSequenceStyleGenerator newStyleGenerator() {
		return new OptimizedSequenceStyleGenerator() {
			@Override
			protected DatabaseStructure buildDatabaseStructure(Type type, Properties params,
					JdbcEnvironment jdbcEnvironment, boolean forceTableUse, QualifiedName sequenceName,
					int initialValue, int incrementSize) {
				return Mockito.mock(DatabaseStructure.class);
			}

		};
	}

	private ServiceRegistry newConfigurationService() {
		ConfigurationService mock = Mockito.mock(ConfigurationService.class);
		Mockito.doReturn(true).when(mock).getSetting(AvailableSettings.PREFER_GENERATOR_NAME_AS_DEFAULT_SEQUENCE_NAME,
				StandardConverters.BOOLEAN, true);
		ServiceRegistry registry = Mockito.mock(ServiceRegistry.class);
		Mockito.doReturn(mock).when(registry).getService(ConfigurationService.class);
		return registry;
	}

	private JdbcEnvironment newJdbcEnvironment() {

		JdbcEnvironment jdbcEnvironment = Mockito.mock(JdbcEnvironment.class);
		IdentifierHelper identifierHelper = Mockito.mock(IdentifierHelper.class);
		Mockito.when(identifierHelper.toIdentifier(ArgumentMatchers.anyString())).then(new Answer<Identifier>() {

			@Override
			public Identifier answer(InvocationOnMock invocation) {
				if (invocation.getArguments()[0] == null)
					return null;
				return new Identifier((String) invocation.getArguments()[0], false);
			}
		});
		Mockito.when(jdbcEnvironment.getIdentifierHelper()).thenReturn(identifierHelper);
		return jdbcEnvironment;
	}

	private ServiceRegistry newServiceRegistry() {
		JdbcEnvironment jdbcEnvironment = newJdbcEnvironment();
		ServiceRegistry serviceRegistry = Mockito.mock(ServiceRegistry.class);
		Mockito.when(serviceRegistry.getService(JdbcEnvironment.class)).thenReturn(jdbcEnvironment);
		Mockito.when(jdbcEnvironment.getDialect()).thenReturn(new MySQL55Dialect());
		return serviceRegistry;
	}

	/**
	 * Check the sequence name from identity table name.
	 */
	@Test
	public void testSequenceName() {
		final Properties params = new Properties();
		params.setProperty("identity_tables", "my_table");
		System.setProperty("hibernate.new_sequence_naming", "true");
		Assertions.assertEquals("my_table_SEQ", newStyleGenerator()
				.determineSequenceName(params, new MySQL55Dialect(), newJdbcEnvironment(), newConfigurationService())
				.getObjectName().getText());
	}

	/**
	 * Check the sequence name from identity table name.
	 */
	@Test
	public void testSequenceNameQuoted() {
		final Properties params = new Properties();
		params.setProperty("identity_tables", "my_table");
		System.setProperty("hibernate.new_sequence_naming", "true");
		Assertions.assertEquals("my_table_SEQ", newStyleGenerator()
				.determineSequenceName(params, new MySQL55Dialect(), newJdbcEnvironment(), newConfigurationService())
				.getObjectName().getText());
	}

	@AfterEach
	public void clearProperty() {
		System.clearProperty("hibernate.new_sequence_naming");
	}
}
