/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

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
class JpaBenchDaoTest extends AbstractBootTest {

	/**
	 * JPA DAO provider for performance.
	 */
	@Autowired
	private ISystemPerformanceJpaDao jpaDao;

	/**
	 * Test for transaction mode.
	 */
	@Test
	void testJpa() {
		jpaDao.initialize(10, null);
	}

	/**
	 * Test for transaction mode with ~10M LOB file.
	 */
	@Test
	void testJpaBlob() throws Exception {
		final var jarLocation = getBlobFile();
		// Get the JAR input
		try (var openStream = jarLocation.openStream()) {
			final var byteArray = IOUtils.toByteArray(openStream);

			// Proceed to the test
			jpaDao.initialize(10, byteArray);
		}
	}

	/**
	 * Test for transaction read mode with ~10M LOB file.
	 */
	@Test
	void testJpaBlobRead() throws Exception {
		// Create blob
		final var jarLocation = getBlobFile();
		// Get the JAR input
		try (var openStream = jarLocation.openStream()) {
			final var byteArray = IOUtils.toByteArray(openStream);

			// Push one entity with our blob
			jpaDao.initialize(1, byteArray);

			// Read and check size
			final var firstAvailableLob = jpaDao.getLastAvailableLob();
			Assertions.assertNotNull(firstAvailableLob);
			Assertions.assertEquals(byteArray.length, firstAvailableLob.length);
		}
	}

	/**
	 * Test for transaction read mode with no blob file.
	 */
	@Test
	void testJpaNoBlobRead() {
		// Push one entity without blob
		jpaDao.initialize(1, null);

		// Read and check size
		final var firstAvailableLob = jpaDao.getLastAvailableLob();
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
