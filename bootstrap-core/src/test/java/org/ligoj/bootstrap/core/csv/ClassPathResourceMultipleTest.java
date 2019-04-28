/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.dao.csv.ClassPathResourceMultiple;

/**
 * Test class of {@link ClassPathResourceMultiple}
 */
public class ClassPathResourceMultipleTest {

	@Test
	public void testGetInputStream2Paths() throws IOException {
		final var resource = new ClassPathResourceMultiple("csv/demo", DummyEntity.class);
		try (var stream = resource.getInputStream()) {
			Assertions.assertNotNull(stream);
		}
	}

	@Test
	public void testGetInputStream1Path() throws IOException {
		final var resource = new ClassPathResourceMultiple("csv/demo", dummyminus.class);
		try (var stream = resource.getInputStream()) {
			Assertions.assertNotNull(stream);
		}
	}

	@Test
	public void testGetInputStreamNotFound() {
		final var resource = new ClassPathResourceMultiple("csv/demo", DummyEntity3.class);
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			try (var stream = resource.getInputStream()) {
				// Should not happen
			}
		});
	}
}
