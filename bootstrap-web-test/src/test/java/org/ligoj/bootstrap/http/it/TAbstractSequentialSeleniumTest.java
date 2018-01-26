package org.ligoj.bootstrap.http.it;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.opentest4j.AssertionFailedError;

/**
 * Test of {@link AbstractSequentialSeleniumTest}
 */
public class TAbstractSequentialSeleniumTest extends AbstractSequentialSeleniumTest {

	private static final String ORIGINAL_ENV = System.getProperty("test.selenium.remote");

	/**
	 * Mocked web driver
	 */
	private WebDriver mockDriver;

	@Test
	public void testSetup() throws Exception {
		System.setProperty("test.selenium.remote", "any");
		super.setUpDriver();
	}

	/**
	 * Run sequentially the given runnable instance.
	 */
	@Test
	public void testSequential() {
		super.runSequential();
	}

	@Override
	protected void runSequential() {
		// Nothing to do
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
	 * Run test.
	 */
	public void mockTestError() {
		throw new AssertionFailedError();
	}

	/**
	 * Run sequentially (one instance) the given runnable instance with an error.
	 */
	@Test
	public void testSequentialError() {
		testName = Mockito.mock(TestInfo.class);
		Mockito.when(testName.getTestMethod())
				.thenReturn(Optional.ofNullable(MethodUtils.getMatchingMethod(this.getClass(), "mockTestError")));
		mockDriver = Mockito.mock(WebDriver.class);
		Mockito.when(mockDriver.manage()).thenThrow(new AssertionFailedError());
		Assertions.assertThrows(AssertionError.class, () -> {
			super.runSequential();
		});
	}

	/**
	 * Run sequentially the given runnable instance with an error.
	 */
	@Test
	public void testSequentialError2() {
		testName = Mockito.mock(TestInfo.class);
		Mockito.when(testName.getTestMethod())
				.thenReturn(Optional.ofNullable(MethodUtils.getMatchingMethod(this.getClass(), "mockTestError")));
		Assertions.assertThrows(AssertionError.class, () -> {
			super.runSequential();
		});
	}

	/**
	 * Create the driver instance
	 */
	@Override
	@BeforeEach
	public void setUpDriver() throws Exception {
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		prepareDriver();
	}

	@AfterEach
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
		((TAbstractSequentialSeleniumTest) target).mockDriver = mockDriver;
		super.cloneAndRun(target, mockDriver, capability);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void prepareDriver() throws Exception {
		testName = Mockito.mock(TestInfo.class);
		Mockito.when(testName.getTestMethod()).thenReturn(Optional.ofNullable(MethodUtils.getMatchingMethod(this.getClass(), "mockTest")));
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		remoteDriverClass = WebDriverMock.class.getName();
		scenario = "sc";
		super.prepareDriver();
		mockDriver = Mockito.mock(WebDriverMock.class);
		Mockito.when(((WebDriverMock) mockDriver).getScreenshotAs(ArgumentMatchers.any(OutputType.class)))
				.thenReturn(new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()));
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
