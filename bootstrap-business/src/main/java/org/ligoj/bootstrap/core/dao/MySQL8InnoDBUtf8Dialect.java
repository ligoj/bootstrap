/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

/**
 * MySQL dialect with enforced UTF-8 encoding.
 */
public class MySQL8InnoDBUtf8Dialect extends MySQL8Dialect {

	@Override
	public String getTableTypeString() {
		return super.getTableTypeString() + " DEFAULT CHARSET=utf8";
	}

	@Override
	public NameQualifierSupport getNameQualifierSupport() {
		return NameQualifierSupport.NONE;
	}

}
