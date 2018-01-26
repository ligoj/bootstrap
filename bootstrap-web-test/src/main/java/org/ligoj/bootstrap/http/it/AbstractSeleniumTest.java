package org.ligoj.bootstrap.http.it;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Common Selenium test class, provides convenient methods to interact with browsers.
 */
public abstract class AbstractSeleniumTest extends AbstractSeleniumQueryTest {

	/**
	 * Login to the application with a sample form login
	 */
	protected void login() {
		getElement(By.id("username-ajax")).clear();
		getElement(By.id("username-ajax")).sendKeys("user");
		getElement(By.id("password-ajax")).clear();
		getElement(By.id("password-ajax")).sendKeys("secret!#1");
		getElement(By.id("submit")).click();
		getElement(By.linkText("Home"));
		getElement(By.linkText("Management"));
	}

	/**
	 * Login to the application with a sample form logout
	 */
	protected void logout() {
		getElement(By.id("_username")).click();
		getElement(By.linkText("Log out")).click();
	}

	/**
	 * Assert the given URL equals to the given one.
	 */
	protected void asserUrl(final String url) {
		Assertions.assertEquals(driver.getCurrentUrl(), url);
	}

	/**
	 * Assert that the multiselect select2 have the given selected values.
	 * 
	 * @param expectedValues
	 *            Form [value1, value2, ...] ([] for no selected values)
	 * @param select2Id
	 *            Id of the select2
	 */
	protected void assertSelect2Values(final String expectedValues, final String select2Id) {
		Assertions.assertEquals(expectedValues, arrayToString(select2GetValues(select2Id)));
	}

	/**
	 * Transform a list in a format : [element1,element2]. No element : []
	 * 
	 * @param elements
	 *            Elements
	 * @return result string
	 */
	protected String arrayToString(final List<String> elements) {
		return ArrayUtils.toString(elements).replace(", ", ",");
	}

	/**
	 * Use this method instead of {link assertInputValue} for input which value is set in DOM (mainly forms)
	 * 
	 * @param expectedText
	 *            Expected text
	 * @param inputId
	 *            Input Id
	 */
	protected void assertDomInputValue(final String expectedText, final String inputId) {
		new WebDriverWait(driver, timeout)
				.until(d -> expectedText.equals(((JavascriptExecutor) driver).executeScript("return $('#" + inputId + "').val();", "")));
	}

	/**
	 * Assert that the given input has the expected text. Warning, don't work for input in a form. For this case use
	 * {link assertDomInputValue}
	 * 
	 * @param expectedText
	 *            Expected text
	 * @param by
	 *            Input location
	 */
	protected void assertInputValue(final String expectedText, final By by) {
		assertElementAttribute(expectedText, by, "value");
	}

	/**
	 * Wait for the given cell being in expected error
	 * 
	 * @param tableId
	 *            Id of the field.
	 * @param expectedError
	 *            Expected error text (Helper.ERROR_*)
	 * @param row
	 *            Table row.
	 * @param column
	 *            Table column.
	 */
	protected void assertCellError(final String expectedError, final String tableId, final int row, final int column) {
		new WebDriverWait(driver, timeout).until(d -> {
			try {
				return driver.findElement(findCell(tableId, row, column)).getAttribute("title").equals(expectedError);
			} catch (final StaleElementReferenceException ex) { // NOSONAR - Not yet ready element, try later
				return false;
			}
		});
	}

	/**
	 * Wait for the given cell having the given text
	 * 
	 * @param tableId
	 *            Id of the field.
	 * @param expectedText
	 *            Expected text.
	 * @param row
	 *            Table row.
	 * @param column
	 *            Table column.
	 */
	protected void assertCellText(final String expectedText, final String tableId, final int row, final int column) {
		new WebDriverWait(driver, timeout).until(d -> {
			try {
				return getElement(findCell(tableId, row, column)).getText().equals(expectedText);
			} catch (final StaleElementReferenceException ex) { // NOSONAR - Not yet ready element, try later
				return false;
			}
		});
	}

	/**
	 * Wait for the given field being in error
	 * 
	 * @param elementId
	 *            Id of the field
	 * @param expectedError
	 *            Expected error text (Helper.ERROR_*)
	 */
	protected void assertFieldError(final String expectedError, final String elementId) {
		new WebDriverWait(driver, timeout).until(d -> {
			try {
				return driver.findElement(findControlGroup(elementId)).getAttribute("title").equals(expectedError);
			} catch (final StaleElementReferenceException ex) { // NOSONAR - Not yet ready element, try later
				return false;
			}
		});
	}

	/**
	 * Wait for the given element to have the given attribute value
	 * 
	 * @param expectedAttributeValue
	 *            Expected attribute value
	 * @param by
	 *            Element location
	 * @param attribute
	 *            Attribute to evaluate
	 */
	protected void assertElementAttribute(final String expectedAttributeValue, final By by, final String attribute) {
		new WebDriverWait(driver, timeout).until(d -> Optional.ofNullable(driver.findElement(by)).map(
				e -> Optional.ofNullable(e.getAttribute(attribute)).map(a -> a.equals(expectedAttributeValue)).orElse(expectedAttributeValue == null))
				.orElse(false));
	}

	/**
	 * Wait for the given element to have the given text
	 * 
	 * @param by
	 *            Element to wait for
	 * @param expectedText
	 *            Waiting value
	 */
	protected void assertElementText(final String expectedText, final By by) {
		new WebDriverWait(driver, timeout).until(d -> {
			try {
				return Optional.ofNullable(driver.findElement(by)).map(WebElement::getText).filter(expectedText::equals).isPresent();
			} catch (final StaleElementReferenceException ex) { // NOSONAR - Not yet ready element, try later
				return false;
			}
		});
	}

	/**
	 * Wait for the given element to be hidden on screen
	 * 
	 * @param by
	 *            Element to wait for
	 */
	protected void assertElementHidden(final By by) {
		new WebDriverWait(driver, timeout).until(d -> {
			try {
				return driver.findElements(by).stream().noneMatch(WebElement::isDisplayed);
			} catch (final StaleElementReferenceException ex) { // NOSONAR - Not yet ready element, try later
				return false;
			}
		});
	}

}
