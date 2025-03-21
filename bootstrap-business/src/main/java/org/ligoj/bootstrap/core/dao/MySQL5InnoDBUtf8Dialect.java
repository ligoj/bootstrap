/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.dialect.DatabaseVersion;

/**
 * "MySql" dialect with enforced UTF-8 encoding.
 */
public class MySQL5InnoDBUtf8Dialect extends AbstractMySQLInnoDBUtDialect {

	/**
	 * Default constructor with additional registered keywords.
	 */
	@SuppressWarnings("this-escape")
	public MySQL5InnoDBUtf8Dialect() {
		super(DatabaseVersion.make(5));
	}
}
