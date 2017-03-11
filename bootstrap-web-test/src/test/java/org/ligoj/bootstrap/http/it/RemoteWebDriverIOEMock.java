package org.ligoj.bootstrap.http.it;

import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Non instantiable web driver.
 */
public class RemoteWebDriverIOEMock extends RemoteWebDriver {

	/**
	 * Remote web driver constructor.
	 * 
	 * @param remoteAddress
	 *            remote address of grid, only there for design requirements of Selenium.
	 * @param desiredCapabilities
	 *            required capabilities, only there for design requirements of Selenium.
	 */
	public RemoteWebDriverIOEMock(final URL remoteAddress, final Capabilities desiredCapabilities) {
		super();
	}

	@Override
	public Capabilities getCapabilities() {
		throw new IllegalThreadStateException();
	}

	@Override
	public void quit() {
		throw new IllegalThreadStateException();
	}
}
