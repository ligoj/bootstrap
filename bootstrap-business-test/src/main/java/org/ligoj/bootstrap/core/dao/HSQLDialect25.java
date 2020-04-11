/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.dialect.HSQLDialect;

/**
 * HSQL dialect with keyword registration.
 */
public class HSQLDialect25 extends HSQLDialect {

	public HSQLDialect25() {
		super();
		registerKeyword("PERIOD");
	}
}
