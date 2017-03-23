package org.ligoj.bootstrap.core.dao;

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

}
