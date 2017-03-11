package org.ligoj.bootstrap.http.it;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.rules.TestName;
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

	@org.junit.Test
	public void testCloneAndRun() throws Exception {
		testName = Mockito.mock(TestName.class);
		Mockito.when(testName.getMethodName()).thenReturn("mockTest");
		cloneAndRun(new Test(), null, null);
	}

	@org.junit.Test
	public void testPrepareBrowser() {
		new TAbstractRepeatableSeleniumTest().prepareBrowser();
	}

	@org.junit.Test
	public void testRepeatMode() throws Exception {
		Assert.assertFalse(isRepeatMode());
		Assert.assertTrue(this.getClass().newInstance().isRepeatMode());
	}

	/**
	 * Create the driver instance
	 */
	@Override
	@Before
	public void setUpDriver() throws Exception {
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		super.setUpDriver();
	}

	@After
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
		Mockito.when(mockDriver.getScreenshotAs(ArgumentMatchers.any(OutputType.class))).thenReturn(
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
