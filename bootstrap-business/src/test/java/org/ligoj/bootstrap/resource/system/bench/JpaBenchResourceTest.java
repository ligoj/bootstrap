/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.bench;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.ws.rs.core.StreamingOutput;

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

/**
 * Test class of {@link JpaBenchResource}.
 */
@ExtendWith(SpringExtension.class)
public class JpaBenchResourceTest extends AbstractBootTest {

	/**
	 * Bench resource for performance.
	 */
	@Autowired
	private JpaBenchResource resource;

	/**
	 * Test for transaction mode with many data.
	 */
	@Test
	public void testPrepareData() throws IOException {
		final int nbEntries = 10;
		assertResult(new ObjectMapperTrim().readValue(resource.prepareData(null, nbEntries), BenchResult.class), nbEntries);
		testCrud(nbEntries);
	}

	/**
	 * Test for transaction mode with 3M LOB file.
	 */
	@Test
	public void testPrepareDataBlob() throws Exception {
		final URL jarLocation = getBlobFile();
		// Get the JAR input
		try (InputStream openStream = jarLocation.openStream()) {
			// Proceed to the test
			final int nbEntries = 10;
			resource.prepareData(openStream, nbEntries);
			testCrud(nbEntries);
		}
	}

	@Test
	public void testDownloadDataBlobEmpty() throws Exception {
		final URL jarLocation = getBlobFile();
		// Get the JAR input
		try (InputStream openStream = jarLocation.openStream()) {

			// Proceed to the test
			resource.prepareData(openStream, 0);
			Assertions.assertNull(resource.downloadLobFile());
		}
	}

	@Test
	public void testDownloadDataBlobError() throws Exception {
		final URL jarLocation = getBlobFile();
		// Get the JAR input
		try (InputStream openStream = jarLocation.openStream()) {

			// Proceed to the test
			resource.prepareData(openStream, 1);
			final StreamingOutput downloadLobFile = resource.downloadLobFile();
			final OutputStream output = Mockito.mock(OutputStream.class);
			Mockito.doThrow(new IOException()).when(output).write(ArgumentMatchers.any(byte[].class));

			Assertions.assertThrows(IllegalStateException.class, () -> {
				downloadLobFile.write(output);
			});
		}
	}

	@Test
	public void testDownloadDataBlob() throws Exception {
		final URL jarLocation = getBlobFile();
		// Get the JAR input
		try (InputStream openStream = jarLocation.openStream()) {

			// Proceed to the test
			resource.prepareData(openStream, 1);
			final StreamingOutput downloadLobFile = resource.downloadLobFile();
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
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
