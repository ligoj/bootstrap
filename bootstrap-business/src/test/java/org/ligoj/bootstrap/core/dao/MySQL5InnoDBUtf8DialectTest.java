/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao;

import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class of {@link MySQL5InnoDBUtf8Dialect}
 */
public class MySQL5InnoDBUtf8DialectTest {

	@Test
	public void getTableTypeString() {
		Assertions.assertEquals(" engine=InnoDB DEFAULT CHARSET=utf8", new MySQL5InnoDBUtf8Dialect().getTableTypeString());
	}

	@Test
	public void getNameQualifierSupport() {
		Assertions.assertEquals(NameQualifierSupport.NONE, new MySQL5InnoDBUtf8Dialect().getNameQualifierSupport());
	}

}
