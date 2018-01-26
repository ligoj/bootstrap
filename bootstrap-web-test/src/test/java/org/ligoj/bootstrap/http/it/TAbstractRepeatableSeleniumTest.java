package org.ligoj.bootstrap.http.it;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebElement;

/**
 * Test of {@link AbstractRepeatableSeleniumTest}
 */
public class TAbstractRepeatableSeleniumTest extends AbstractRepeatableSeleniumTest {

	private static final String ORIGINAL_ENV = System.getProperty("test.selenium.remote");

	private WebDriverMock mockDriver;

	public class Test extends AbstractRepeatableSeleniumTest {

		/**
		 * Run test.
		 */
		public void mockTest() {
			// Nothing to do
		}

		@Override
		protected void prepareBrowser() {
			// Nothing to do
		}

	}

	@org.junit.jupiter.api.Test
	public void testCloneAndRun() throws Exception {
		testName = Mockito.mock(TestInfo.class);
		Mockito.when(testName.getTestMethod()).thenReturn(Optional.ofNullable(MethodUtils.getMatchingMethod(this.getClass(), "mockTest")));
		cloneAndRun(new Test(), null, null);
	}

	@org.junit.jupiter.api.Test
	public void testPrepareBrowser() {
		new TAbstractRepeatableSeleniumTest().prepareBrowser();
	}

	@org.junit.jupiter.api.Test
	public void testRepeatMode() throws Exception {
		Assertions.assertFalse(isRepeatMode());
		Assertions.assertTrue(this.getClass().newInstance().isRepeatMode());
	}

	/**
	 * Create the driver instance
	 */
	@Override
	@BeforeEach
	public void setUpDriver() throws Exception {
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		super.setUpDriver();
	}

	@AfterEach
	public void restoreProperties() {
		if (ORIGINAL_ENV == null) {
			System.clearProperty("test.selenium.remote");
		} else {
			System.setProperty("test.selenium.remote", ORIGINAL_ENV);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void prepareDriver() throws Exception {
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		remoteDriverClass = WebDriverMock.class.getName();
		scenario = "sc";
		super.prepareDriver();
		mockDriver = Mockito.mock(WebDriverMock.class);
		Mockito.when(mockDriver.getScreenshotAs(ArgumentMatchers.any(OutputType.class)))
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
