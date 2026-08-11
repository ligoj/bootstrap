/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Test of {@link AbstractTest}
 */
class TestAbstractTest extends AbstractTest {

	@Test
	void testCloseQuietly() throws IOException {
		final var mock = mock(Closeable.class);
		closeQuietly(mock);
		verify(mock, atLeastOnce()).close();
	}

	@Test
	void testCloseQuietlyClosed() throws IOException {
		final var mock = mock(Closeable.class);
		doThrow(new IOException()).when(mock).close();
		closeQuietly(mock);
		verify(mock, atLeastOnce()).close();
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
