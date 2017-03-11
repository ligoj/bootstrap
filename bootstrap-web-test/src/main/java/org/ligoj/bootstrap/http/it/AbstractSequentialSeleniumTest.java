package org.ligoj.bootstrap.http.it;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Test class base supporting sequential executions on different browsers.
 */
@Slf4j
public abstract class AbstractSequentialSeleniumTest extends AbstractRepeatableSeleniumTest {

	@Before
	@Override
	public void setUpDriver() throws Exception { // NOPMD -- too much exception
		super.setUpDriver();
		runSequential();
	}

	/**
	 * Sequentially run the desired capabilities.
	 */
	protected void runSequential() { // NOPMD -- thread
		final WebDriver[] drivers = new WebDriver[repeatedCapabilities.length];
		final boolean[] success = new boolean[repeatedCapabilities.length];
		for (int index = 0; index < repeatedCapabilities.length; index++) {
			final int driverIndex = index;
			final DesiredCapabilities capability = repeatedCapabilities[driverIndex];
			success[driverIndex] = false;
			runSequentialIndex(drivers, success, driverIndex, capability);
		}

		final List<String> faillures = checkResults(success);
		Assert.assertTrue("All browsers test failed", faillures.size() != success.length);
		Assert.assertEquals("Some browsers test failed", success.length, success.length - faillures.size());
	}

	/**
	 * Check the results.
	 */
	private List<String> checkResults(final boolean... success) {
		final List<String> faillures = new ArrayList<>();
		for (int index = 0; index < success.length; index++) {
			if (!success[index]) {
				faillures.add(repeatedCapabilities[index].getBrowserName() + "[" + repeatedCapabilities[index].getVersion() + "]/"
						+ repeatedCapabilities[index].getPlatform());
			}
		}
		return faillures;
	}

	/**
	 * Run a capability.
	 */
	private void runSequentialIndex(final WebDriver[] drivers, final boolean[] success, final int driverIndex, final DesiredCapabilities capability) {
		AbstractSequentialSeleniumTest seleniumTest = this;
		try {
			final WebDriver driver = getRemoteDriver(capability);
			drivers[driverIndex] = driver;
			seleniumTest = this.getClass().newInstance();
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
