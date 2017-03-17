package org.ligoj.bootstrap.http.it;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Test class base supporting repeated executions.
 */
public abstract class AbstractRepeatableSeleniumTest extends AbstractSeleniumTest {

	/**
	 * Test name.
	 */
	@Rule
	// CHECKSTYLE:OFF
	public TestName testName = new TestName();
	// CHECKSTYLE:ON

	// As default, Firefox, IE and Chrome navigators
	protected static final DesiredCapabilities[] DEFAULT_CAPABILITIES = new DesiredCapabilities[] { DesiredCapabilities.firefox(),
			DesiredCapabilities.internetExplorer(), DesiredCapabilities.chrome() };

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
	 */
	protected void cloneAndRun(final AbstractRepeatableSeleniumTest target, final WebDriver driver, final DesiredCapabilities capability)
			throws Exception { // NOSONAR -- Have to pause the thread
		target.gridUrl = gridUrl;
		target.baseDir = baseDir;
		target.baseUrl = baseUrl;
		target.scenario = scenario;
		target.capability = capability;
		target.driver = driver;
		target.prepareBrowser();
		target.getClass().getMethod(testName.getMethodName()).invoke(target);
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
		return testName.getMethodName() == null;
	}
}
