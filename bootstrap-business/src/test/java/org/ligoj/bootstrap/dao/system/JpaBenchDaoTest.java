/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JPA bench test.
 */
@ExtendWith(SpringExtension.class)
public class JpaBenchDaoTest extends AbstractBootTest {

	/**
	 * JPA DAO provider for performance.
	 */
	@Autowired
	private ISystemPerformanceJpaDao jpaDao;

	/**
	 * Test for transaction mode.
	 */
	@Test
	public void testJpa() {
		jpaDao.initialize(10, null);
	}

	/**
	 * Test for transaction mode with 3M LOB file.
	 */
	@Test
	public void testJpaBlob() throws Exception {
		final URL jarLocation = getBlobFile();
		// Get the JAR input
		try (InputStream openStream = jarLocation.openStream()) {
			final byte[] byteArray = IOUtils.toByteArray(openStream);

			// Proceed to the test
			jpaDao.initialize(10, byteArray);
		}
	}

	/**
	 * Test for transaction read mode with 3M LOB file.
	 */
	@Test
	public void testJpaBlobRead() throws Exception {
		// Create blob
		final URL jarLocation = getBlobFile();
		// Get the JAR input
		try (InputStream openStream = jarLocation.openStream()) {
			final byte[] byteArray = IOUtils.toByteArray(openStream);

			// Push one entity with our blob
			jpaDao.initialize(1, byteArray);

			// Read and check size
			final byte[] firstAvailableLob = jpaDao.getLastAvailableLob();
			Assertions.assertNotNull(firstAvailableLob);
			Assertions.assertEquals(byteArray.length, firstAvailableLob.length);
		}
	}

	/**
	 * Test for transaction read mode with no blob file.
	 */
	@Test
	public void testJpaNoBlobRead() {
		// Push one entity without blob
		jpaDao.initialize(1, null);

		// Read and check size
		final byte[] firstAvailableLob = jpaDao.getLastAvailableLob();
		Assertions.assertNotNull(firstAvailableLob);
		Assertions.assertEquals(0, firstAvailableLob.length);
	}

	/**
	 * Get a Blob file URL.
	 */
	private URL getBlobFile() throws ClassNotFoundException {
		return Class.forName("org.hibernate.Hibernate").getProtectionDomain().getCodeSource().getLocation();
	}
}
