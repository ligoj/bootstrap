/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;

/**
 * Check JSR-303 features. Also check over @Service beans.
 * 
 */
class ValidationTest {
	/**
	 * Validator instance.
	 */
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	void upperCase() {
		final var wine = newWine();
		var validate = validator.validate(wine);
		Assertions.assertEquals(0, validate.size());
		wine.setUpper("lower");
		validate = validator.validate(wine);
		Assertions.assertEquals(1, validate.size());
		final ConstraintViolation<?> constraintViolation = validate.iterator().next();
		Assertions.assertEquals("lower", constraintViolation.getInvalidValue());
		Assertions.assertEquals(wine, constraintViolation.getLeafBean());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.UpperCase.message",
				constraintViolation.getMessageTemplate());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.UpperCase.message",
				constraintViolation.getMessage());
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	void lowerCase() {
		final var wine = newWine();
		var validate = validator.validate(wine);
		Assertions.assertEquals(0, validate.size());
		wine.setLower("UPPER");
		validate = validator.validate(wine);
		Assertions.assertEquals(1, validate.size());
		final ConstraintViolation<?> constraintViolation = validate.iterator().next();
		Assertions.assertEquals("UPPER", constraintViolation.getInvalidValue());
		Assertions.assertEquals(wine, constraintViolation.getLeafBean());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.LowerCase.message",
				constraintViolation.getMessageTemplate());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.LowerCase.message",
				constraintViolation.getMessage());
	}

	@Test
	void safeHtmlNull() {
		final var wine = newWine();
		wine.setSafe(null);
		var validate = validator.validate(wine);
		Assertions.assertEquals(0, validate.size());
	}

	@Test
	void lxCaseEmpty() {
		final var wine = newWine();
		wine.setLower("");
		wine.setUpper(null);
		var validate = validator.validate(wine);
		Assertions.assertEquals(0, validate.size());
	}

	@Test
	void safeHtml() {
		final var wine = newWine();
		var validate = validator.validate(wine);
		Assertions.assertEquals(0, validate.size());
		wine.setSafe("<img src='data:image/png;base64,100101' href='http://foo'/>");
		validate = validator.validate(wine);
		Assertions.assertEquals(1, validate.size());
		wine.setSafe("<img src='/some/relative/url/image.png' />");
		validate = validator.validate(wine);
		Assertions.assertEquals(0, validate.size());
		wine.setSafe("<td>1234qwer</td>");
		validate = validator.validate(wine);
		Assertions.assertEquals(0, validate.size());
		wine.setSafe("<script>some</script>");
		validate = validator.validate(wine);
		Assertions.assertEquals(1, validate.size());
		final ConstraintViolation<?> constraintViolation = validate.iterator().next();
		Assertions.assertEquals("<script>some</script>", constraintViolation.getInvalidValue());
		Assertions.assertEquals(wine, constraintViolation.getLeafBean());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.SafeHtml.message",
				constraintViolation.getMessageTemplate());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.SafeHtml.message",
				constraintViolation.getMessage());
	}

	private class SampleBean {

		@LowerCase
		@Getter
		@Setter
		private String lower;

		@UpperCase
		@Getter
		@Setter
		private String upper;

		@SafeHtml
		@Getter
		@Setter
		private String safe;

	}

	/**
	 * Create a valid entity.
	 */
	private SampleBean newWine() {
		final var wine = new SampleBean();
		wine.setUpper("C");
		wine.setLower("p");
		wine.setSafe("<a href=\"#/home\">link</a>");
		return wine;
	}
}
