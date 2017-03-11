package org.ligoj.bootstrap.http.it;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.ScreenshotException;

/**
 * Test of {@link AbstractSeleniumTest}
 */
public class TAbstractSeleniumTest extends AbstractSeleniumTest {

	private static final String ORIGINAL_ENV = System.getProperty("test.selenium.remote");

	/**
	 * Mocked web driver
	 */
	private WebDriverMock mockDriver;

	/**
	 * Mock sleep failure.
	 */
	private boolean failSleep = false;

	@Test
	public void testCleanupClean() {
		cleanup();
		cleanup();
	}

	@Test
	public void testExtractScreenShot() {
		Assert.assertNull(extractScreenShot(new WebDriverException()));
	}

	@Test
	public void testExtractScreenShot2() {
		final ScreenshotException se = Mockito.mock(ScreenshotException.class);
		Mockito.when(se.getBase64EncodedScreenshot()).thenReturn("ex");
		Assert.assertEquals("ex", extractScreenShot(new WebDriverException(se)));
	}

	@Test
	public void testScreenShotInvalidFile() {
		scenario = null;
		screenshot("");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testScreenShot2() throws URISyntaxException, IOException {
		try {
			Mockito.when(mockDriver.getScreenshotAs(ArgumentMatchers.any(OutputType.class)))
					.thenReturn(new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()));
			screenshot("test.png");
			Assert.assertTrue(new File(baseDir, "sc/firefox/001-test.png").exists());
		} finally {
			FileUtils.deleteDirectory(new File(baseDir, "sc"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testScreenShotIOE() throws URISyntaxException, IOException {
		try {
			Mockito.when(mockDriver.getScreenshotAs(ArgumentMatchers.any(OutputType.class)))
					.thenReturn(new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()).getParentFile());
			screenshot("test.png");
			Assert.assertFalse(new File(baseDir, "sc/test.png").exists());
		} finally {
			FileUtils.deleteDirectory(new File(baseDir, "sc"));
		}
	}

	@Test
	public void testSetup() throws Exception {
		System.setProperty("test.selenium.remote", "any");
		super.setUpDriver();
	}

	@Test
	public void testPrepareDriverRemote() throws Exception {
		System.setProperty("test.selenium.remote", "any");
		super.prepareDriver();
	}

	@Test
	public void testPrepareDriverLocal() throws Exception {
		System.clearProperty("test.selenium.remote");
		super.prepareDriver();
	}

	@Test
	public void testPrepareBrowser() {
		System.setProperty("test.selenium.remote", "any");
		super.prepareBrowser();
	}

	@Test
	public void testPrepareBrowserLocal() {
		System.clearProperty("test.selenium.remote");
		super.prepareBrowser();
	}

	@Test
	public void testLogin() {
		login();
	}

	@Test
	public void testLogout() {
		logout();
	}

	@Test
	public void testAsserUrl() {
		Mockito.when(mockDriver.getCurrentUrl()).thenReturn("url");
		asserUrl("url");
	}

	@Test
	public void testConnect() {
		connect();
	}

	@Test
	public void testAssertSelectedText() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getTagName()).thenReturn("select");
		final List<WebElement> items = new ArrayList<>();
		items.add(webElement);
		Mockito.when(webElement.findElements(ArgumentMatchers.any(By.class))).thenReturn(items);
		Mockito.when(webElement.isSelected()).thenReturn(true);
		Mockito.when(webElement.getText()).thenReturn("text");
		assertSelectedText("text", null);
	}

	@Test
	public void testAssertSelect2Values() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement).thenThrow(new NoSuchElementException(""));
		Mockito.when(webElement.getTagName()).thenReturn("select");
		final List<WebElement> items = new ArrayList<>();
		items.add(webElement);
		Mockito.when(webElement.findElements(ArgumentMatchers.any(By.class))).thenReturn(items);
		Mockito.when(webElement.isSelected()).thenReturn(true);
		Mockito.when(webElement.getText()).thenReturn("text");
		assertSelect2Values("[text]", null);
	}

	@Test
	public void testSelectOptionByText() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement).thenThrow(new NoSuchElementException(""));
		Mockito.when(webElement.getTagName()).thenReturn("select");
		final List<WebElement> items = new ArrayList<>();
		items.add(webElement);
		Mockito.when(webElement.findElements(ArgumentMatchers.any(By.class))).thenReturn(items);
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		Mockito.when(webElement.isSelected()).thenReturn(true);
		Mockito.when(webElement.getText()).thenReturn("text");
		selectOptionByText(null, "text");
	}

	@Test
	public void testAssertDomInputValue() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement).thenThrow(new NoSuchElementException(""));
		Mockito.when(((JavascriptExecutor) mockDriver).executeScript(ArgumentMatchers.any(String.class), ArgumentMatchers.any())).thenReturn("text");
		assertDomInputValue("text", "a");
	}

	@Test
	public void testAssertInputValue() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(null).thenReturn(webElement);
		assertInputValue(null, null);
	}

	@Test
	public void testAssertElementAttribute() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.anyString())).thenReturn(null).thenReturn("value");
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		assertElementAttribute("value", null, null);
	}

	@Test
	public void testAssertElementAttribute2() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.anyString())).thenReturn("value");
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		assertElementAttribute("value", null, null);
	}

	@Test
	public void testAssertElementText() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.getText()).thenThrow(new StaleElementReferenceException("")).thenReturn("value");
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		assertElementText("value", null);
	}

	@Test
	public void testAssertElementHidden() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException("")).thenReturn(true).thenReturn(false);
		final List<WebElement> items = new ArrayList<>();
		items.add(webElement);
		items.add(webElement);
		Mockito.when(mockDriver.findElements(ArgumentMatchers.any(By.class))).thenReturn(items);
		assertElementHidden(null);
	}

	@Test
	public void testWaitFixedTime() {
		final Options options = mockDriver.manage();
		Mockito.when(options.timeouts()).thenReturn(Mockito.mock(Timeouts.class));
		waitFixedTime();
	}

	@Test
	public void testAssertTableEmpty() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getText()).thenThrow(new StaleElementReferenceException("")).thenReturn("Aucune donnée disponible dans le tableau");
		assertTableEmpty("a");
	}

	@Test
	public void testAssertFieldError() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.anyString())).thenThrow(new StaleElementReferenceException("")).thenReturn("error");
		assertFieldError("error", "a");
	}

	@Test
	public void testAssertCellText() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getText()).thenThrow(new StaleElementReferenceException("")).thenReturn("error");
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		assertCellText("error", "a", 0, 0);
	}

	@Test
	public void testAssertCellError() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.anyString())).thenThrow(new StaleElementReferenceException("")).thenReturn("error");
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		assertCellError("error", "a", 0, 0);
	}

	@Test
	public void testGetElement() {
		final WebElement webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.isDisplayed()).thenReturn(false).thenReturn(true);
		getElement(null);
	}

	@Test
	public void testSmallSleepError() {
		failSleep = true;
		smallSleep();
	}

	@Test
	public void testSmallSleep() {
		smallSleep();
	}

	@Test
	public void testSelect2RemoveValue() {
		select2RemoveValue("a", "b");
	}

	@Test
	public void testSelect2SelectValue() {
		select2SelectValue("a", 1);
	}

	@Test
	public void testSelect2SelectValue2() {
		select2SelectValue("a", "b");
	}

	@Test
	public void covertUtils() {
		findControlGroup("a");
		findCellChild("a", 0, 0);
		findCell("a", 0, 0);
		findErrorInTable("a", 0, 0);
		final List<String> elements = new ArrayList<>();
		elements.add("a");
		elements.add("a");
		Assert.assertEquals("[a,a]", arrayToString(elements));
	}

	@Override
	protected void sleep(final long milli) throws InterruptedException {
		if (failSleep) {
			throw new InterruptedException();
		}
		super.sleep(milli);
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
		failSleep = false;
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
}
