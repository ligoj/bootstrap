/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.it;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Test class base supporting repeated executions.
 */
public abstract class AbstractRepeatableSeleniumTest extends AbstractSeleniumTest {

	protected TestInfo testName;

	/**
	 * Test name.
	 * 
	 * @param testInfo
	 *            Injected test context.
	 */
	@BeforeEach
	public void initTestName(final TestInfo testInfo) {
		testName = testInfo;
	}

	// As default, Firefox, IE and Chrome navigators
	protected static final DesiredCapabilities[] DEFAULT_CAPABILITIES = new DesiredCapabilities[] {
			DesiredCapabilities.firefox(), DesiredCapabilities.internetExplorer(), DesiredCapabilities.chrome() };

	/**
	 * Repeated capabilities.
	 */
	protected DesiredCapabilities[] repeatedCapabilities = DEFAULT_CAPABILITIES;

	/**
	 * Copy environment to forked thread.
	 * 
	 * @param target
	 *            the test to be repeated.
	 * @param driver
	 *            the driver instance to use for this forked test.
	 * @param capability
	 *            the associated capabilities of this forked test.
	 * @throws Exception
	 *             When text invoke fails.
	 */
	protected void cloneAndRun(final AbstractRepeatableSeleniumTest target, final WebDriver driver,
			final DesiredCapabilities capability) throws Exception { // NOSONAR -- Have to pause the thread
		target.gridUrl = gridUrl;
		target.baseDir = baseDir;
		target.baseUrl = baseUrl;
		target.scenario = scenario;
		target.capability = capability;
		target.driver = driver;
		target.prepareBrowser();
		target.getClass().getMethod(testName.getTestMethod().get().getName()).invoke(target);
	}

	@SuppressWarnings("all")
	@Override
	protected void prepareDriver() throws Exception { // NOPMD -- inheritance
		// Nothing to do locally
	}

	@Override
	protected void prepareBrowser() {
		if (driver != null) {
			// Forked process
			super.prepareBrowser();
		}
	}

	/**
	 * Indicates if the current thread is running in forked test.
	 * 
	 * @return <tt>true</tt> when the current thread is running in forked test.
	 */
	protected boolean isRepeatMode() {
		return testName == null || !testName.getTestMethod().isPresent();
	}
}
