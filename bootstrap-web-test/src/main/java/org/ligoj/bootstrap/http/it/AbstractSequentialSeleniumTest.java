/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.it;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import lombok.extern.slf4j.Slf4j;

/**
 * Test class base supporting sequential executions on different browsers.
 */
@Slf4j
public abstract class AbstractSequentialSeleniumTest extends AbstractRepeatableSeleniumTest {

	@BeforeEach
	@Override
	void setUpDriver() throws Exception { // NOPMD -- too much exception
		super.setUpDriver();
		runSequential();
	}

	/**
	 * Sequentially run the desired capabilities.
	 */
    void runSequential() { // NOPMD -- thread
		final var drivers = new WebDriver[repeatedCapabilities.length];
		final var success = new boolean[repeatedCapabilities.length];
		for (var index = 0; index < repeatedCapabilities.length; index++) {
			final var capability = repeatedCapabilities[index];
			success[index] = false;
			runSequentialIndex(drivers, success, index, capability);
		}

		final var failures = checkResults(success);
		Assertions.assertTrue(failures.size() != success.length, "All browsers test failed");
		Assertions.assertEquals(0, failures.size(), "Some browsers test failed");
	}

	/**
	 * Check the results.
	 */
	private List<String> checkResults(final boolean... success) {
		final List<String> failures = new ArrayList<>();
		for (var index = 0; index < success.length; index++) {
			if (!success[index]) {
				failures.add(repeatedCapabilities[index].getBrowserName() + "[" + repeatedCapabilities[index].getVersion() + "]/"
						+ repeatedCapabilities[index].getPlatform());
			}
		}
		return failures;
	}

	/**
	 * Run a capability.
	 */
	private void runSequentialIndex(final WebDriver[] drivers, final boolean[] success, final int driverIndex,
			final DesiredCapabilities capability) {
		var seleniumTest = this;
		try {
			final var driver = getRemoteDriver(capability);
			drivers[driverIndex] = driver;
			seleniumTest = this.getClass().getDeclaredConstructor().newInstance();
			cloneAndRun(seleniumTest, driver, capability);
			success[driverIndex] = true;
		} catch (final Exception e) {
			log.error("Unable to build the driver for requested capability, other tests are not interrupted : " + capability, e);
		} finally {
			cleanup();
			seleniumTest.cleanup();
		}
	}

}
