/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.boot.model.relational.QualifiedName;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * An extension of {@link org.hibernate.jpa.spi.IdentifierGeneratorStrategyProvider} allowing to set globally the
 * configuration of {@link SequenceStyleGenerator} behavior such as
 * {@link SequenceStyleGenerator#CONFIG_PREFER_SEQUENCE_PER_ENTITY} and {@link SequenceStyleGenerator#INCREMENT_PARAM}
 */
public class SequenceIdentifierGeneratorStrategyProvider
		implements org.hibernate.jpa.spi.IdentifierGeneratorStrategyProvider {

	private final Map<String, Class<?>> strategies = new HashMap<>();

	/**
	 * Simple constructor registering strategies.
	 */
	public SequenceIdentifierGeneratorStrategyProvider() {
		strategies.put("enhanced-sequence", OptimizedSequenceStyleGenerator.class);
		strategies.put(SequenceStyleGenerator.class.getName(), OptimizedSequenceStyleGenerator.class);
	}

	@Override
	public Map<String, Class<?>> getStrategies() {
		return strategies;
	}

	/**
	 * A simple proxy of {@link SequenceStyleGenerator#configure(Type, Properties, ServiceRegistry)} where our global
	 * sequence settings are placed.
	 */
	public static class OptimizedSequenceStyleGenerator extends SequenceStyleGenerator {

		@Override
		public void configure(final Type type, final Properties params, final ServiceRegistry serviceRegistry) {
			// Augment the property set with the global configuration
			params.setProperty(CONFIG_PREFER_SEQUENCE_PER_ENTITY, Boolean.TRUE.toString());
			params.setProperty(INCREMENT_PARAM, "1000");
			params.setProperty(FORCE_TBL_PARAM, "true");
			super.configure(type, params, serviceRegistry);
		}

		@Override
		protected QualifiedName determineSequenceName(final Properties params, final Dialect dialect,
				final JdbcEnvironment jdbcEnv, final ServiceRegistry serviceRegistry) {
			// Make sure sequence are lower case and corresponds to table name
			params.put(SEQUENCE_PARAM, StringHelper.unquote(params.getProperty("identity_tables"))
					+ ConfigurationHelper.getString(CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, params, DEF_SEQUENCE_SUFFIX));
			return super.determineSequenceName(params, dialect, jdbcEnv, serviceRegistry);
		}
	}

}
