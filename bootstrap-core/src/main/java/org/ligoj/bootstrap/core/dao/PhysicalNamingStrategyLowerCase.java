/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import java.util.Locale;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Strategy forcing lower case for all data names.
 */
public class PhysicalNamingStrategyLowerCase extends org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy {
}
