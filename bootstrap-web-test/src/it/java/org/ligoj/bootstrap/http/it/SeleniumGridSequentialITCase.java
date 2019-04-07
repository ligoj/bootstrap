/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.it;

import org.junit.jupiter.api.Test;
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
	 * @return <code>true</code> for local test. <code>false</code> other wise : Grid test
	 */
	@Override
	protected boolean isLocalTest() {
		return false;
	}
}