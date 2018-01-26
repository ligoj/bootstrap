package org.ligoj.bootstrap.http.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Test phantom JS plugability. Test class of {@link AbstractSeleniumTest}
 */
public class TAbstractSeleniumITCase extends AbstractSeleniumTest {

	/**
	 * Create the driver instance
	 */
	@Override
	protected void prepareDriver() throws Exception {
		if (!isLocalTest()) {
			capability = DesiredCapabilities.phantomjs();
		}
		super.prepareDriver();
	}

	@Test
	public void testExtractScreenShot() {
		driver.get("https://www.google.com");
		Assertions.assertEquals("Google", driver.getTitle());
	}
}
