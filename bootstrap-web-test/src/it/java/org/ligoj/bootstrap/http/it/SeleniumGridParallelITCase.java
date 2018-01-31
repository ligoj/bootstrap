package org.ligoj.bootstrap.http.it;

import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Integration test with Selenium grid feature.
 */
public class SeleniumGridParallelITCase extends AbstractParallelSeleniumTest {

	public SeleniumGridParallelITCase() {
		// Add phantomjs in test
		super.repeatedCapabilities = new DesiredCapabilities[] { DesiredCapabilities.phantomjs(), DesiredCapabilities.phantomjs() };
	}

	@Test
	public void testParallelePing() {
		if (isRepeatMode()) {
			SeleniumGridParallelITCase.this.scenario = "ping";
			driver.get("https://www.google.com");
			screenshot("ping.png");
		}
	}

}