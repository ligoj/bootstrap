package org.ligoj.bootstrap.http.it;

import java.io.File;
import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Test of {@link AbstractParallelSeleniumTest}
 */
public class TAbstractParallelSeleniumTest extends AbstractParallelSeleniumTest {

	private static final String ORIGINAL_ENV = System.getProperty("test.selenium.remote");

	/**
	 * Shared sleep value.
	 */
	private static long sleep;

	/**
	 * Mocked web driver
	 */
	private WebDriver mockDriver;

	/**
	 * Mock sleep failure.
	 */
	private boolean failSleep = false;

	@Test
	public void testSetup() throws Exception {
		System.setProperty("test.selenium.remote", "any");
		super.setUpDriver();
	}

	/**
	 * Run asynchronously the given runnable instance.
	 */
	@Test
	public void testParallel() {
		super.runParallel();
	}

	/**
	 * Run sequentially (one instance) the given runnable instance with an exception.
	 */
	@Test(expected = AssertionError.class)
	public void testParallelIOE() {
		testName = Mockito.mock(TestName.class);
		Mockito.when(testName.getMethodName()).thenReturn("mockTestIOE");
		super.runParallel();
	}

	/**
	 * Run asynchronously the given runnable instance with an error.
	 */
	@Test(expected = AssertionError.class)
	public void testParallelRunningError() {
		testName = Mockito.mock(TestName.class);
		Mockito.when(testName.getMethodName()).thenThrow(new AssertionFailedError());
		super.runParallel();
	}

	/**
	 * Run asynchronously the given runnable instance with an error during the initialization.
	 */
	@Test(expected = AssertionError.class)
	public void testParallelInitializationError() {
		localDriverClass = RemoteWebDriverMock.class.getName();
		remoteDriverClass = RemoteWebDriverMock.class.getName();
		super.runParallel();
	}

	/**
	 * Run asynchronously the given runnable instance with an error during the initialization.
	 */
	@Test(expected = IllegalStateException.class)
	public void testParallelInitializationErrorNotCaught() {
		localDriverClass = RemoteWebDriverMock.class.getName();
		remoteDriverClass = RemoteWebDriverMock.class.getName();

		final DesiredCapabilities mockCapability = Mockito.mock(DesiredCapabilities.class);
		Mockito.when(mockCapability.toString()).thenThrow(new IllegalStateException());
		repeatedCapabilities = new DesiredCapabilities[] { mockCapability };

		super.runParallel();
	}

	/**
	 * Run asynchronously the given runnable instance with an error.
	 */
	@Test(expected = AssertionError.class)
	public void testParallelIOE3() {
		mockDriver = Mockito.mock(RemoteWebDriver.class);
		localDriverClass = RemoteWebDriverIOEMock.class.getName();
		remoteDriverClass = RemoteWebDriverIOEMock.class.getName();
		super.runParallel();
	}

	/**
	 * Run asynchronously the given runnable instance with an error.
	 */
	@Test(expected = AssertionError.class)
	public void testParallelIOE4() {
		testName = Mockito.mock(TestName.class);
		Mockito.when(testName.getMethodName()).thenReturn("mockTestIOE");
		final DesiredCapabilities mockCapability = Mockito.mock(DesiredCapabilities.class);
		Mockito.when(mockCapability.getBrowserName()).thenThrow(new IllegalStateException());
		repeatedCapabilities = new DesiredCapabilities[] { mockCapability };
		super.runParallel();
	}

	/**
	 * Run test.
	 */
	public void mockTestParallelLong() throws InterruptedException {
		Thread.sleep(getNextSleep());
	}

	/**
	 * Run asynchronously the given runnable instance with abnormally long run.
	 */
	@Test(expected = AssertionError.class)
	public void testParallelLong() {
		sleep = 0;
		maxRetry = 3;
		testName = Mockito.mock(TestName.class);
		Mockito.when(testName.getMethodName()).thenReturn("mockTestParallelLong");
		super.runParallel();
	}

	@Override
	public void runParallel() {
		// Nothing to do
	}

	@Override
	protected void sleep(final long milli) throws InterruptedException {
		if (failSleep) {
			throw new InterruptedException();
		}
		super.sleep(milli);
	}

	/**
	 * Used to distribute different sleep values.
	 */
	private long getNextSleep() {
		synchronized (TAbstractParallelSeleniumTest.class) {
			sleep += 1000;
			return sleep;
		}
	}

	/**
	 * Run test.
	 */
	public void mockTest() {
		// Nothing to do
	}

	/**
	 * Run test.
	 */
	public void mockTestIOE() throws IOException {
		throw new IOException();
	}

	/**
	 * Create the driver instance
	 */
	@Override
	@Before
	public void setUpDriver() throws Exception {
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		failSleep = false;
		prepareDriver();
	}

	@After
	public void restoreProperties() {
		if (ORIGINAL_ENV == null) {
			System.clearProperty("test.selenium.remote");
		} else {
			System.setProperty("test.selenium.remote", ORIGINAL_ENV);
		}
	}

	@Override
	protected void cloneAndRun(final AbstractRepeatableSeleniumTest target, final WebDriver driver, final DesiredCapabilities capability)
			throws Exception { // NOPMD -- too many
		((TAbstractParallelSeleniumTest) target).mockDriver = mockDriver;
		super.cloneAndRun(target, mockDriver, capability);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void prepareDriver() throws Exception {
		testName = Mockito.mock(TestName.class);
		Mockito.when(testName.getMethodName()).thenReturn("mockTest");
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		remoteDriverClass = WebDriverMock.class.getName();
		failSleep = false;
		scenario = "sc";
		super.prepareDriver();
		mockDriver = Mockito.mock(WebDriverMock.class);
		Mockito.when(((WebDriverMock) mockDriver).getScreenshotAs(ArgumentMatchers.any(OutputType.class))).thenReturn(
				new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()));
		final Options options = Mockito.mock(Options.class);
		Mockito.when(options.window()).thenReturn(Mockito.mock(Window.class));
		Mockito.when(mockDriver.manage()).thenReturn(options);
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		this.driver = mockDriver;
	}

	@Override
	protected void prepareBrowser() {
		driver = mockDriver;
		super.prepareBrowser();
	}
}
