package org.ligoj.bootstrap.core.dao;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * MySQL dialect with enforced UTF-8 encoding.
 */
public class MySQL5InnoDBUtf8Dialect extends MySQL5InnoDBDialect {

	@Override
	public String getTableTypeString() {
		return super.getTableTypeString() + " DEFAULT CHARSET=utf8";
	}

}
