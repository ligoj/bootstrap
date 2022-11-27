/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.io.Closeable;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test of {@link AbstractTest}
 */
class TestAbstractTest extends AbstractTest {

	@Test
	void testCloseQuietly() throws IOException {
		final var mock = Mockito.mock(Closeable.class);
		closeQuietly(mock);
		Mockito.verify(mock, Mockito.atLeastOnce()).close();
	}

	@Test
	void testCloseQuietlyClosed() throws IOException {
		final var mock = Mockito.mock(Closeable.class);
		Mockito.doThrow(new IOException()).when(mock).close();
		closeQuietly(mock);
		Mockito.verify(mock, Mockito.atLeastOnce()).close();
	}

	@Test
	void testCloseNull() {
		closeQuietly(null);
	}

	@Test
	void testCheckPermission() {
		EXIT_DETECTOR.start();
	}

}
