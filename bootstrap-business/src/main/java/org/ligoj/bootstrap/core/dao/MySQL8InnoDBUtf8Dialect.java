/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

/**
 * "MySql" dialect with enforced UTF-8 encoding.
 */
public class MySQL8InnoDBUtf8Dialect extends MySQLDialect {

	/**
	 * Default constructor with additional registered keywords.
	 */
	@SuppressWarnings({"this-escape"})
	public MySQL8InnoDBUtf8Dialect() {
		super( DatabaseVersion.make( 8 ) );
		registerKeyword("CUME_DIST");
		registerKeyword("DENSE_RANK");
		registerKeyword("EMPTY");
		registerKeyword("EXCEPT");
		registerKeyword("FIRST_VALUE");
		registerKeyword("GROUPS");
		registerKeyword("JSON_TABLE");
		registerKeyword("LAG");
		registerKeyword("LAST_VALUE");
		registerKeyword("LEAD");
		registerKeyword("NTH_VALUE");
		registerKeyword("NTILE");
		registerKeyword("PERSIST");
		registerKeyword("PERCENT_RANK");
		registerKeyword("PERSIST_ONLY");
		registerKeyword("RANK");
		registerKeyword("ROW_NUMBER");
		registerKeyword("USAGE");
	}

	@Override
	public String getTableTypeString() {
		return super.getTableTypeString() + " DEFAULT CHARSET=utf8";
	}

	@Override
	public NameQualifierSupport getNameQualifierSupport() {
		return NameQualifierSupport.NONE;
	}

}
