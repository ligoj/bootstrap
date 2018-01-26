package org.ligoj.bootstrap.http.it;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import lombok.extern.slf4j.Slf4j;

/**
 * Test class base supporting parallel executions on different browsers.
 */
@Slf4j
public abstract class AbstractParallelSeleniumTest extends AbstractRepeatableSeleniumTest {

	/**
	 * Retries count for slow browsers do not responding while at least on has
	 * already finished its execution.
	 */
	private static final int DEFAULT_MAX_RETRY = 100;

	/**
	 * Thread alive check, in millisecond.
	 */
	private static final int DEFAULT_ALIVE_CHECK = 100;

	/**
	 * Thread alive check, in millisecond.
	 */
	protected int aliveCheck = DEFAULT_ALIVE_CHECK;

	/**
	 * Retries count for slow browsers do not responding while at least on has
	 * already finished its execution.
	 */
	protected int maxRetry = DEFAULT_MAX_RETRY;

	@BeforeEach
	@Override
	public void setUpDriver() throws Exception { // NOSONAR -- too much exception
		super.setUpDriver();
		runParallel();
	}

	/**
	 * Run asynchronously the current test, and join their execution.
	 */
	protected void runParallel() {
		final WebDriver[] drivers = new WebDriver[repeatedCapabilities.length];
		final boolean[] success = new boolean[repeatedCapabilities.length];
		final Thread[] threads = prepareThreads(drivers, success); // NOPMD -- thread

		// Start the browsers
		final int finished = startThreads(threads);

		// Monitor the browsers
		final List<String> faillures = new ArrayList<>();
		try {
			monitorThreads(threads, finished, success, drivers);

			checkResults(success, faillures);
		} catch (final Exception e) {
			log.error("Weird Exception during the run ...", e);
			faillures.add("Weird Exception during the run ...");
		}
		Assertions.assertTrue(faillures.size() != success.length, "All browsers test failed");
		Assertions.assertEquals(0, faillures.size(), "Some browsers test failed");
	}

	/**
	 * Check the results.
	 */
	private void checkResults(final boolean[] success, final List<String> faillures) {
		for (int index = 0; index < success.length; index++) {
			if (!success[index]) {
				faillures.add(repeatedCapabilities[index].getBrowserName() + "[" + repeatedCapabilities[index].getVersion() + "]/"
						+ repeatedCapabilities[index].getPlatform());
			}
		}
	}

	/**
	 * Monitor the threads
	 */
	private void monitorThreads(final Thread[] threads, final int startedThreads, // NOPMD NOSONAR -- thread
			final boolean[] success, final WebDriver... drivers) throws InterruptedException {
		final State state = new State();
		state.finished = startedThreads;
		state.retryCount = 0;
		state.succeed = 0;
		while (state.finished != threads.length) {
			for (int index = 0; index < threads.length; index++) {
				monitorThread(threads, success, state, index, drivers);
			}
		}
	}

	/**
	 * Monitor a thread
	 */
	private void monitorThread(final Thread[] threads, final boolean[] success, final State state, final int index,
			final WebDriver... drivers) // NOPMD
			throws InterruptedException {
		final Thread thread = threads[index]; // NOPMD -- thread
		if (thread != null) {
			// Check the browser is alive
			thread.join(aliveCheck); // NOPMD -- thread
			if (!thread.isAlive()) { // NOPMD -- thread

				// This browser just finished its test
				state.retryCount = 0;
				state.finished++;
				threads[index] = null;
				if (success[index]) {
					// Finished successfully
					state.succeed++;
				}
			} else if (state.succeed > 0 && ++state.retryCount > maxRetry) {
				// Not yet finished
				// And yet, at least one browser succeed
				// And Give up this browser
				log.error("Weird latency for browser " + repeatedCapabilities[index] + ", giving up");
				state.finished++;
				threads[index] = null;
				drivers[index].quit();
			}
		}
	}

	/**
	 * Thread monitoring state.
	 */
	private static class State {
		private int finished;
		private int retryCount;
		private int succeed;
	}

	/**
	 * Start the given threads.
	 */
	private int startThreads(final Thread... threads) { // NOPMD -- thread
		int finished = 0;
		for (final Thread thread : threads) { // NOPMD -- thread
			if (thread == null) {
				finished++;
			} else {
				thread.start();
			}
		}
		return finished;
	}

	/**
	 * Prepare the thread instances.
	 */
	private Thread[] prepareThreads(final WebDriver[] drivers, final boolean[] success) { // NOPMD
		final Thread[] threads = new Thread[repeatedCapabilities.length]; // NOPMD -- thread
		for (int index = 0; index < repeatedCapabilities.length; index++) {
			final int driverIndex = index;
			final DesiredCapabilities capability = repeatedCapabilities[driverIndex];
			success[driverIndex] = false;
			try {
				final WebDriver driver = getRemoteDriver(capability);
				drivers[driverIndex] = driver;
				threads[driverIndex] = prepareThread(driver, driverIndex, success, capability);
			} catch (final Exception e) {
				log.error("Unable to connect the remote web driver, other tests are not interrupted : " + capability, e);
			} finally {
				cleanup();
			}
		}
		return threads;
	}

	/**
	 * Prepare the thread instance.
	 */
	private Thread prepareThread(final WebDriver driver, final int driverIndex, // NOPMD -- thread
			final boolean[] success, final DesiredCapabilities capability) {
		return new Thread() { // NOPMD -- thread

			@Override
			public void run() {
				AbstractParallelSeleniumTest seleniumTest = AbstractParallelSeleniumTest.this;
				try {
					seleniumTest = AbstractParallelSeleniumTest.this.getClass().getDeclaredConstructor().newInstance();
					AbstractParallelSeleniumTest.this.cloneAndRun(seleniumTest, driver, capability);
					success[driverIndex] = true;
				} catch (final Exception e) {
					log.error("Unable to build the driver for requested capability, other tests are not interrupted : " + capability, e);
				} finally {
					cleanup();
					seleniumTest.cleanup();
				}
			}
		};
	}

}
