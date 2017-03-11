package org.ligoj.bootstrap.http.it;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class WebDriverMock implements WebDriver, JavascriptExecutor, TakesScreenshot {

	/**
	 * Local web driver constructor.
	 */
	public WebDriverMock() {
		// Local mode
	}

	/**
	 * Remote web driver constructor.
	 * 
	 * @param remoteAddress
	 *            remote address of grid, only there for design requirements of Selenium.
	 * @param desiredCapabilities
	 *            required capabilities, only there for design requirements of Selenium.
	 */
	public WebDriverMock(final URL remoteAddress, final Capabilities desiredCapabilities) {
		// Remote mode
	}

	@Override
	public void get(final String url) {
		// nothing to get
	}

	@Override
	public String getCurrentUrl() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public List<WebElement> findElements(final By by) {
		return null;
	}

	@Override
	public WebElement findElement(final By by) {
		return null;
	}

	@Override
	public String getPageSource() {
		return null;
	}

	@Override
	public void close() {
		// Nothing to do
	}

	@Override
	public void quit() {
		// Nothing to do
	}

	@Override
	public Set<String> getWindowHandles() {
		return null;
	}

	@Override
	public String getWindowHandle() {
		return null;
	}

	@Override
	public TargetLocator switchTo() {
		return null;
	}

	@Override
	public Navigation navigate() {
		return null;
	}

	@Override
	public Options manage() {
		return Mockito.mock(Options.class);
	}

	@Override
	public Object executeScript(final String script, final Object... args) {
		return null;
	}

	@Override
	public Object executeAsyncScript(final String script, final Object... args) {
		return null;
	}

	@Override
	public <X> X getScreenshotAs(final OutputType<X> target) {
		return null;
	}

}
