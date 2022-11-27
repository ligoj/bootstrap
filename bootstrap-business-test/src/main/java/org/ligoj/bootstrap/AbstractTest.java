/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.io.Closeable;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import lombok.extern.slf4j.Slf4j;

/**
 * Common test class.
 */
@Slf4j
public class AbstractTest {

	/**
	 * Default mock HTTP port
	 */
	protected static final int MOCK_PORT = 8120;

	protected static final Thread EXIT_DETECTOR = new Thread(() -> log.error("Something called exit within test executions",
			new IllegalStateException("Something called exit within test executions"))
	);

	/**
	 * Initialize mocks of this class.
	 */
	@BeforeEach
	public void injectMock() throws Exception {
		MockitoAnnotations.openMocks(this).close();
	}

	/**
	 * Closes a <code>Closeable</code> unconditionally.
	 * <p>
	 * Equivalent to {@link Closeable#close()}, except any exceptions will be ignored. This is typically used in
	 * finally blocks.
	 * <p>
	 * Example code:
	 * </p>
	 * <pre>
	 * Closeable closeable = null;
	 * try {
	 *     closeable = new FileReader(&quot;foo.txt&quot;);
	 *     // process closeable
	 *     closeable.close();
	 * } catch (Exception e) {
	 *     // error handling
	 * } finally {
	 *     IOUtils.closeQuietly(closeable);
	 * }
	 * </pre>
	 * <p>
	 * Closing all streams:
	 * </p>
	 * <pre>
	 * try {
	 *     return IOUtils.copy(inputStream, outputStream);
	 * } finally {
	 *     IOUtils.closeQuietly(inputStream);
	 *     IOUtils.closeQuietly(outputStream);
	 * }
	 * </pre>
	 *
	 * @param closeable the objects to close, may be null or already closed
	 */
	protected static void closeQuietly(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException ioe) {
			// ignore
		}
	}

	/**
	 * Install a hook to prevent {@link System#exit(int)} to be executed.
	 */
	@BeforeAll
	public static void forbidSystemExitCall() {
		Runtime.getRuntime().addShutdownHook(EXIT_DETECTOR);
	}

	/**
	 * Restore previous security manager replaced by the one installed by {@link #forbidSystemExitCall()}.
	 */
	@AfterAll
	public static void restoreSystemExitCall() {
		Runtime.getRuntime().removeShutdownHook(EXIT_DETECTOR);
	}

}
