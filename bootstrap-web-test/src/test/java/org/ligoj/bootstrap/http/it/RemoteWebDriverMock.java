/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.it;

import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Non instantiable web driver.
 */
public class RemoteWebDriverMock extends RemoteWebDriver {

	/**
	 * Remote web driver constructor.
	 * 
	 * @param remoteAddress
	 *            remote address of grid, only there for design requirements of Selenium.
	 * @param desiredCapabilities
	 *            required capabilities, only there for design requirements of Selenium.
	 */
	public RemoteWebDriverMock(final URL remoteAddress, final Capabilities desiredCapabilities) {
		super();
		throw new RuntimeException("Web driver initialization error");
	}
}
