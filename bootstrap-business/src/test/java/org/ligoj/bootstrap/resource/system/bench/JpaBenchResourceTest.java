/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.bench;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.dao.system.BenchResult;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * Test class of {@link JpaBenchResource}.
 */
@ExtendWith(SpringExtension.class)
class JpaBenchResourceTest extends AbstractBootTest {

	/**
	 * Bench resource for performance.
	 */
	@Autowired
	private JpaBenchResource resource;

	/**
	 * Test for transaction mode with many data.
	 */
	@Test
	void testPrepareData() throws IOException {
		final var nbEntries = 10;
		assertResult(new ObjectMapperTrim().readValue(resource.prepareData(String.valueOf(nbEntries), null), BenchResult.class), nbEntries);
		testCrud(nbEntries);
	}

	/**
	 * Test for transaction mode with 3M LOB file.
	 */
	@Test
	void testPrepareDataBlob() throws Exception {
		final var jarLocation = getBlobFile();
		// Get the JAR input
		try (var openStream = jarLocation.openStream()) {
			// Proceed to the test
			final var nbEntries = 10;
			resource.prepareData(String.valueOf(nbEntries), openStream);
			testCrud(nbEntries);
		}
	}

	@Test
	void testDownloadDataBlobEmpty() throws Exception {
		final var jarLocation = getBlobFile();
		// Get the JAR input
		try (var openStream = jarLocation.openStream()) {

			// Proceed to the test
			resource.prepareData("0", openStream);
			Assertions.assertNull(resource.downloadLobFile());
		}
	}

	@Test
	void testDownloadDataBlobError() throws Exception {
		final var jarLocation = getBlobFile();
		// Get the JAR input
		try (var openStream = jarLocation.openStream()) {

			// Proceed to the test
			resource.prepareData("1", openStream);
			final var downloadLobFile = resource.downloadLobFile();
			final var output = Mockito.mock(OutputStream.class);
			Mockito.doThrow(new IOException()).when(output).write(ArgumentMatchers.any(byte[].class));

			Assertions.assertThrows(IllegalStateException.class, () -> downloadLobFile.write(output));
		}
	}

	@Test
	void testDownloadDataBlob() throws Exception {
		final var jarLocation = getBlobFile();
		// Get the JAR input
		try (var openStream = jarLocation.openStream()) {

			// Proceed to the test
			resource.prepareData("1", openStream);
			final var downloadLobFile = resource.downloadLobFile();
			final var output = new ByteArrayOutputStream();
			downloadLobFile.write(output);
			Assertions.assertTrue(output.toByteArray().length > 3000000);
		}
	}

	/**
	 * Test for deletion in transaction mode with many data.
	 */
	private void testDeleteData(final int nbEntries) {
		assertResult(resource.benchDelete(), nbEntries);
	}

	/**
	 * Test for read in transaction mode with many data.
	 */
	private void testReadData(final int nbEntries) {
		assertResult(resource.benchRead(), nbEntries);
	}

	/**
	 * Test for read all in transaction mode with many data.
	 */
	private void testReadAllData(final int nbEntries) {
		assertResult(resource.benchReadAll(), nbEntries);
	}

	/**
	 * Test for update in transaction mode with many data.
	 */
	private void testUpdateData(final int nbEntries) {
		assertResult(resource.benchUpdate(), nbEntries);
	}

	/**
	 * Execute and check all CRUD operations.O
	 */
	private void testCrud(final int expectedEntries) {
		testReadData(expectedEntries);
		testReadAllData(expectedEntries);
		testUpdateData(expectedEntries);
		testDeleteData(expectedEntries);
	}

	/**
	 * Check the result.
	 */
	private void assertResult(final BenchResult prepareData, final int expectedEntries) {
		Assertions.assertNotNull(prepareData);
		Assertions.assertEquals(expectedEntries, prepareData.getEntries());
		Assertions.assertTrue(prepareData.getDuration() >= 0);
	}

	/**
	 * Get a Blob file URL.
	 */
	private URL getBlobFile() throws ClassNotFoundException {
		return Class.forName("org.hibernate.Hibernate").getProtectionDomain().getCodeSource().getLocation();
	}
}
