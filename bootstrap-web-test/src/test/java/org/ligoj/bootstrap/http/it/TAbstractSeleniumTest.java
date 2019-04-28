/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.it;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

	@Test
    void testCleanupClean() {
		cleanup();
		cleanup();
	}

	@BeforeEach
    void clearTimeout() {
		timeout = 1;
	}

	@Test
    void testExtractScreenShot() {
		Assertions.assertNull(extractScreenShot(new WebDriverException()));
	}

	@Test
    void testExtractScreenShot2() {
		final var se = Mockito.mock(ScreenshotException.class);
		Mockito.when(se.getBase64EncodedScreenshot()).thenReturn("ex");
		Assertions.assertEquals("ex", extractScreenShot(new WebDriverException(se)));
	}

	@Test
    void testScreenShotInvalidFile() {
		scenario = null;
		screenshot("");
	}

	@SuppressWarnings("unchecked")
	@Test
    void testScreenShot2() throws URISyntaxException, IOException {
		try {
			Mockito.when(mockDriver.getScreenshotAs(ArgumentMatchers.any(OutputType.class)))
					.thenReturn(new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()));
			screenshot("test.png");
			Assertions.assertTrue(new File(baseDir, "sc/firefox/001-test.png").exists());
		} finally {
			FileUtils.deleteDirectory(new File(baseDir, "sc"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
    void testScreenShotIOE() throws URISyntaxException, IOException {
		try {
			Mockito.when(mockDriver.getScreenshotAs(ArgumentMatchers.any(OutputType.class)))
					.thenReturn(new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()).getParentFile());
			screenshot("test.png");
			Assertions.assertFalse(new File(baseDir, "sc/test.png").exists());
		} finally {
			FileUtils.deleteDirectory(new File(baseDir, "sc"));
		}
	}

	@Test
    void testSetup() throws Exception {
		System.setProperty("test.selenium.remote", "any");
		super.setUpDriver();
	}

	@Test
    void testPrepareDriverRemote() throws Exception {
		System.setProperty("test.selenium.remote", "any");
		super.prepareDriver();
	}

	@Test
    void testPrepareDriverLocal() throws Exception {
		System.clearProperty("test.selenium.remote");
		super.prepareDriver();
	}

	@Test
    void testPrepareBrowser() {
		System.setProperty("test.selenium.remote", "any");
		super.prepareBrowser();
	}

	@Test
    void testPrepareBrowserLocal() {
		System.clearProperty("test.selenium.remote");
		super.prepareBrowser();
	}

	@Test
    void testLogin() {
		login();
	}

	@Test
    void testLogout() {
		logout();
	}

	@Test
    void testAsserUrl() {
		Mockito.when(mockDriver.getCurrentUrl()).thenReturn("url");
		assertUrl("url");
	}

	@Test
    void testConnect() {
		connect();
	}

	@Test
    void testAssertSelectedText() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any())).thenReturn(webElement);
		Mockito.when(webElement.getTagName()).thenReturn("select");
		final List<WebElement> items = new ArrayList<>();
		items.add(webElement);
		Mockito.when(webElement.findElements(ArgumentMatchers.any())).thenReturn(items);
		Mockito.when(webElement.isSelected()).thenReturn(true);
		Mockito.when(webElement.getText()).thenReturn("text");
		assertSelectedText("text", null);
	}

	@Test
    void testAssertSelect2Values() {
		final var webElement = Mockito.mock(WebElement.class);
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
    void testSelectOptionByText() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any())).thenReturn(webElement).thenThrow(new NoSuchElementException(""));
		Mockito.when(webElement.getTagName()).thenReturn("select");
		final List<WebElement> items = new ArrayList<>();
		items.add(webElement);
		Mockito.when(webElement.findElements(ArgumentMatchers.any())).thenReturn(items);
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		Mockito.when(webElement.isSelected()).thenReturn(true);
		Mockito.when(webElement.getText()).thenReturn("text");
		selectOptionByText(null, "text");
	}

	@Test
    void testAssertDomInputValue() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement).thenThrow(new NoSuchElementException(""));
		Mockito.when(((JavascriptExecutor) mockDriver).executeScript(ArgumentMatchers.any(String.class), ArgumentMatchers.any())).thenReturn("text");
		assertDomInputValue("text", "a");
	}

	@Test
    void testAssertInputValue() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any())).thenReturn(null).thenReturn(webElement);
		assertInputValue(null, null);
	}

	@Test
    void testAssertElementAttribute() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.any())).thenReturn(null).thenReturn("value");
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any())).thenReturn(webElement);
		assertElementAttribute("value", null, null);
	}

	@Test
    void testAssertElementAttribute2() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.any())).thenReturn("value");
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any())).thenReturn(webElement);
		assertElementAttribute("value", null, null);
	}

	@Test
    void testAssertElementText() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.getText()).thenThrow(new StaleElementReferenceException("")).thenReturn("value");
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any())).thenReturn(webElement);
		assertElementText("value", null);
	}

	@Test
    void testAssertElementHidden() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException("")).thenReturn(true).thenReturn(false);
		final List<WebElement> items = new ArrayList<>();
		items.add(webElement);
		items.add(webElement);
		Mockito.when(mockDriver.findElements(ArgumentMatchers.any())).thenReturn(items);
		assertElementHidden(null);
	}

	@Test
    void testWaitFixedTime() {
		final var options = mockDriver.manage();
		Mockito.when(options.timeouts()).thenReturn(Mockito.mock(Timeouts.class));
		waitFixedTime();
	}

	@Test
    void testAssertFieldError() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.anyString())).thenThrow(new StaleElementReferenceException("")).thenReturn("error");
		assertFieldError("error", "a");
	}

	@Test
    void testAssertCellText() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getText()).thenThrow(new StaleElementReferenceException("")).thenReturn("error");
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		assertCellText("error", "a", 0, 0);
	}

	@Test
    void testAssertCellError() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.getAttribute(ArgumentMatchers.anyString())).thenThrow(new StaleElementReferenceException("")).thenReturn("error");
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		assertCellError("error", "a", 0, 0);
	}

	@Test
    void testGetElement() {
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any())).thenReturn(webElement);
		Mockito.when(webElement.isDisplayed()).thenReturn(false).thenReturn(true);
		getElement(null);
	}

	@Test
    void testSmallSleep() throws InterruptedException {
		smallSleep();
	}

	@Test
    void testSelect2RemoveValue() throws InterruptedException {
		select2RemoveValue("a", "b");
	}

	@Test
    void testSelect2SelectValue() throws InterruptedException {
		select2SelectValue("a", 1);
	}

	@Test
    void testSelect2SelectValue2() throws InterruptedException {
		select2SelectValue("a", "b");
	}

	@Test
    void covertUtils() {
		findControlGroup("a");
		findCellChild("a", 0, 0);
		findCell("a", 0, 0);
		findErrorInTable("a", 0, 0);
		final List<String> elements = new ArrayList<>();
		elements.add("a");
		elements.add("a");
		Assertions.assertEquals("[a,a]", arrayToString(elements));
	}

	@AfterEach
    void restoreProperties() {
		if (ORIGINAL_ENV == null) {
			System.clearProperty("test.selenium.remote");
		} else {
			System.setProperty("test.selenium.remote", ORIGINAL_ENV);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
    void prepareDriver() throws Exception {
		System.clearProperty("test.selenium.remote");
		localDriverClass = WebDriverMock.class.getName();
		remoteDriverClass = WebDriverMock.class.getName();
		scenario = "sc";
		super.prepareDriver();
		mockDriver = Mockito.mock(WebDriverMock.class);
		Mockito.when(mockDriver.getScreenshotAs(ArgumentMatchers.any(OutputType.class)))
				.thenReturn(new File(Thread.currentThread().getContextClassLoader().getResource("log4j2.json").toURI()));
		final var options = Mockito.mock(Options.class);
		Mockito.when(options.window()).thenReturn(Mockito.mock(Window.class));
		Mockito.when(mockDriver.manage()).thenReturn(options);
		final var webElement = Mockito.mock(WebElement.class);
		Mockito.when(mockDriver.findElement(ArgumentMatchers.any(By.class))).thenReturn(webElement);
		Mockito.when(webElement.isDisplayed()).thenReturn(true);
		this.driver = mockDriver;
	}
}
