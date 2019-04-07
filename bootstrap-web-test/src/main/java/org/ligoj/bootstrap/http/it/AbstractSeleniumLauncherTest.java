/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.it;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ScreenshotException;

import lombok.extern.slf4j.Slf4j;

/**
 * Common Selenium test class, provides convenient methods to interact with browsers.
 */
@Slf4j
public abstract class AbstractSeleniumLauncherTest {

	/**
	 * UI timeout for availability.
	 */
	protected int timeout = 10;

	/**
	 * Default capability.
	 */
	protected static final DesiredCapabilities DEFAULT_CAPABILITY = DesiredCapabilities.firefox();

	/**
	 * Default local driver.
	 */
	protected static final String DEFAULT_LOCAL_DRIVER = "org.openqa.selenium.firefox.FirefoxDriver";

	/**
	 * Default remote driver.
	 */
	protected static final String DEFAULT_REMOTE_DRIVER = "org.openqa.selenium.remote.RemoteWebDriver";

	/**
	 * Validation error : cannot be empty
	 */
	public static final String ERROR_CANNOT_BE_EMPTY = "Ne peut pas être vide";

	/**
	 * Validation error : lower case
	 */
	public static final String ERROR_LOWERCASE = "Doit être en minuscule";

	/**
	 * Validation error : no error
	 */
	public static final String ERROR_NO_ERROR = "";

	/**
	 * Selenium Hub URL
	 */
	protected static final String GRID_URL = System.getProperty("test.selenium.hub.url",
			ObjectUtils.defaultIfNull(System.getenv("SELENIUM_GRID"), "http://localhost:4444/wd/hub"));

	protected String baseDir = "c:\\tmp\\";

	/**
	 * The local driver class used only for local mode tests.
	 */
	protected String localDriverClass = DEFAULT_LOCAL_DRIVER;

	/**
	 * The remote driver class used only for remote mode tests.
	 */
	protected String remoteDriverClass = DEFAULT_REMOTE_DRIVER;

	protected WebDriver driver;

	protected DesiredCapabilities capability = DEFAULT_CAPABILITY;

	protected String scenario;

	protected String baseUrl;

	protected URL gridUrl;

	/**
	 * Screenshot counter.
	 */
	protected int screenshotCounter = 0;

	/**
	 * @return <code>true</code> for local test. <code>false</code> other wise : Grid test
	 */
	protected boolean isLocalTest() {
		return System.getProperty("test.selenium.remote") == null;
	}

	/**
	 * Return a new {@link org.openqa.selenium.remote.RemoteWebDriver} instance.
	 * 
	 * @param capability
	 *            the desired capabilities.
	 * @return the remote {@link WebDriver}
	 * @throws Exception
	 *             from driver loader and many lock management.
	 */
	protected WebDriver getRemoteDriver(final DesiredCapabilities capability) throws Exception { // NOSONAR -- too many
																									// exception
		log.info("Asking for " + capability + " to " + gridUrl);
		return new Augmenter().augment((WebDriver) Class.forName(remoteDriverClass)
				.getConstructor(URL.class, Capabilities.class).newInstance(gridUrl, capability));
	}

	/**
	 * Return a new local {@link WebDriver} instance.
	 * 
	 * @return A new local {@link WebDriver} instance.
	 * @throws Exception
	 *             from driver loader and many lock management.
	 */
	protected WebDriver getLocalDriver() throws Exception { // NOSONAR -- too many exception
		return (WebDriver) Class.forName(localDriverClass).getDeclaredConstructor().newInstance();
	}

	/**
	 * Create the driver instance
	 * 
	 * @throws Exception
	 *             from driver loader and many lock management.
	 */
	@BeforeEach
	public void setUpDriver() throws Exception { // NOSONAR -- too many exception
		gridUrl = new URL(GRID_URL);
		baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI())
				.getParent();
		scenario = "undefined";
		if (isLocalTest()) {
			this.baseUrl = System.getProperty("test.local.server.url", "http://localhost");
		} else {
			this.baseUrl = System.getProperty("test.target.server.url",
					"http://" + InetAddress.getLocalHost().getHostAddress() + ":80"); // NOPMD
		}
		prepareDriver();
		prepareBrowser();
	}

	/**
	 * Prepare the driver.
	 * 
	 * @throws Exception
	 *             from driver loader and many lock management.
	 */
	protected void prepareDriver() throws Exception { // NOSONAR -- too many exception
		if (isLocalTest()) {
			driver = getLocalDriver();
		} else {
			driver = getRemoteDriver(capability);
		}
	}

	/**
	 * Quit the current driver.
	 */
	@AfterEach
	public void cleanup() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}

	/**
	 * Ensure all messages have been sent by waiting for some seconds.
	 * 
	 * @throws InterruptedException
	 *             from {@link Thread#sleep(long)}
	 */
	@AfterAll
	public static void ensureCleanup() throws InterruptedException {
		Thread.sleep(2000); // NOSONAR -- Have to pause the thread
	}

	/**
	 * Prepare the driver for the launch
	 */
	protected void prepareBrowser() {
		driver.manage().deleteAllCookies();
		driver.manage().window().maximize();
	}

	protected String extractScreenShot(final WebDriverException e) {
		final Throwable cause = e.getCause();
		if (cause instanceof ScreenshotException) {
			return ((ScreenshotException) cause).getBase64EncodedScreenshot();
		}
		return null;
	}

	/**
	 * Take a screenshot after a short time and save the content to the given sub directory.
	 * 
	 * @param to
	 *            Simple file name where the screenshot will be saved.
	 */
	protected void screenshot(final String to) {
		try {
			sleep(750);
			screenshotNow(to);
		} catch (final Exception ioe) {
			log.error("Unable to take screenshot of URL '" + driver.getCurrentUrl() + "'", ioe);
		}
	}

	/**
	 * Take a screenshot immediately and save the content to the given sub directory.
	 * 
	 * @param to
	 *            Simple file name where the screenshot will be saved.
	 * @throws IOException
	 *             When screenshot cannot be saved.
	 */
	protected void screenshotNow(final String to) throws IOException {
		final File directory = new File(new File(baseDir, scenario), capability.getBrowserName());
		FileUtils.forceMkdir(directory);
		try {
			// Copy the received screenshot to the target directory
			final File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			final File targetFile = new File(directory,
					StringUtils.leftPad(String.valueOf(++screenshotCounter), 3, '0') + "-" + to);
			log.info("Screenshot received : '" + scrFile + ", copying to " + targetFile);
			FileUtils.copyFile(scrFile, targetFile);
		} catch (final IOException ioe) {
			log.error("Unable to copy screenshot of URL '" + driver.getCurrentUrl() + "' to given file '" + to + "'",
					ioe);
		}
	}

	/**
	 * Connect to the application until the login page
	 */
	protected void connect() {
		driver.get(baseUrl);
	}

	/**
	 * Wait 1s. Useful for waiting for component graphical transition
	 * @throws InterruptedException
	 *             From {@link Thread#sleep(long)}
	 */
	protected void smallSleep() throws InterruptedException {
		sleep(1000);
	}

	/**
	 * Sleep implementation.
	 * 
	 * @param milli
	 *            the length of time to sleep in milliseconds
	 * @throws InterruptedException
	 *             From {@link Thread#sleep(long)}
	 */
	protected void sleep(final long milli) throws InterruptedException {
		Thread.sleep(milli); // NOSONAR -- Have to pause the thread
	}
}
