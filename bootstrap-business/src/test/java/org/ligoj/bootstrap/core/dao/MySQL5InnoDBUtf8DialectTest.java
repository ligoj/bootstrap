package org.ligoj.bootstrap.core.dao;

import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class of {@link MySQL5InnoDBUtf8Dialect}
 */
public class MySQL5InnoDBUtf8DialectTest {

	@Test
	public void getTableTypeString() {
		Assert.assertEquals(" engine=InnoDB DEFAULT CHARSET=utf8", new MySQL5InnoDBUtf8Dialect().getTableTypeString());
	}

	@Test
	public void getNameQualifierSupport() {
		Assert.assertEquals(NameQualifierSupport.NONE, new MySQL5InnoDBUtf8Dialect().getNameQualifierSupport());
	}

}
