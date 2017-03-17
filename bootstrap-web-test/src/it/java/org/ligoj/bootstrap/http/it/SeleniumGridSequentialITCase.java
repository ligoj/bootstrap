package org.ligoj.bootstrap.http.it;

import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Integration test with Selenium grid feature.
 */
public class SeleniumGridSequentialITCase extends AbstractSequentialSeleniumTest {

	public SeleniumGridSequentialITCase() {
		// Add phantomjs in test
		super.repeatedCapabilities = new DesiredCapabilities[] { DesiredCapabilities.phantomjs(), DesiredCapabilities.phantomjs() };
	}

	@Test
	public void testSequentialLogin() {
		if (isRepeatMode()) {
			SeleniumGridSequentialITCase.this.scenario = "ping";
			driver.get("http://www.google.com");
			screenshot("ping.png");
		}
	}

	/**
	 * @return <tt>true</tt> for local test. <tt>false</tt> other wise : Grid test
	 */
	@Override
	protected boolean isLocalTest() {
		return false;
	}
}