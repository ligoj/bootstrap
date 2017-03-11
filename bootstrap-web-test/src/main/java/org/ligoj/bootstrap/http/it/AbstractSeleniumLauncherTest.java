package org.ligoj.bootstrap.http.it;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ScreenshotException;

/**
 * Common Selenium test class, provides convenient methods to interact with browsers.
 */
@Slf4j
public abstract class AbstractSeleniumLauncherTest {

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
	 * @return <tt>true</tt> for local test. <tt>false</tt> other wise : Grid test
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
	protected WebDriver getRemoteDriver(final DesiredCapabilities capability) throws Exception { // NOPMD -- throws
		log.info("Asking for " + capability + " to " + gridUrl);
		return new Augmenter().augment((WebDriver) Class.forName(remoteDriverClass).getConstructor(URL.class, Capabilities.class)
				.newInstance(gridUrl, capability));
	}

	/**
	 * Return a new local {@link WebDriver} instance.
	 * 
	 * @throws Exception
	 *             from driver loader and many lock management.
	 */
	protected WebDriver getLocalDriver() throws Exception { // NOPMD -- too much exception
		return (WebDriver) Class.forName(localDriverClass).newInstance();
	}

	/**
	 * Create the driver instance
	 * 
	 * @throws Exception
	 *             from driver loader and many lock management.
	 */
	@Before
	public void setUpDriver() throws Exception { // NOPMD -- too much exception
		gridUrl = new URL(GRID_URL);
		baseDir = new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()).getParent();
		scenario = "undefined";
		if (isLocalTest()) {
			this.baseUrl = System.getProperty("test.local.server.url", "http://localhost");
		} else {
			this.baseUrl = System.getProperty("test.target.server.url", "http://" + InetAddress.getLocalHost().getHostAddress() + ":80"); // NOPMD
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
	protected void prepareDriver() throws Exception { // NOPMD -- too much exception
		if (isLocalTest()) {
			driver = getLocalDriver();
		} else {
			driver = getRemoteDriver(capability);
		}
	}

	/**
	 * Quit the current driver.
	 */
	@After
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
	@AfterClass
	public static void ensureCleanup() throws InterruptedException {
		Thread.sleep(2000);
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
	 * Take a screenshot and save the content to the given sub directory.
	 * 
	 * @param to
	 *            Simple file name where the screenshot will be saved.
	 */
	protected void screenshot(final String to) {
		try {
			final File directory = new File(new File(baseDir, scenario), capability.getBrowserName());
			sleep(750);
			FileUtils.forceMkdir(directory);
			try {
				// Copy the received screenshot to the target directory
				final File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				final File targetFile = new File(directory, StringUtils.leftPad(String.valueOf(++screenshotCounter), 3, '0') + "-" + to);
				log.info("Screenshot received : '" + scrFile + ", copying to " + targetFile);
				FileUtils.copyFile(scrFile, targetFile);
			} catch (final IOException ioe) {
				log.error("Unable to copy screenshot of URL '" + driver.getCurrentUrl() + "' to given file '" + to + "'", ioe);
			}
		} catch (final Exception ioe) {
			log.error("Unable to take screenshot of URL '" + driver.getCurrentUrl() + "'", ioe);
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
	 */
	protected void smallSleep() { // Means string ?
		try {
			sleep(1000);
		} catch (final InterruptedException ex) {
			log.error("Interrupted test", ex);
		}
	}

	/**
	 * Sleep implementation.
	 * 
	 * @param millis
	 *            the length of time to sleep in milliseconds
	 * @throws InterruptedException
	 *             from {@link Thread#sleep(long)}
	 */
	protected void sleep(final long milli) throws InterruptedException {
		Thread.sleep(milli);
	}
}
