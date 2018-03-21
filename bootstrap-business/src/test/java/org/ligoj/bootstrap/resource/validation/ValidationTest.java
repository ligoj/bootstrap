/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.validation;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.validation.LowerCase;
import org.ligoj.bootstrap.core.validation.ValidatorBean;
import org.ligoj.bootstrap.core.validation.Wine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.Getter;
import lombok.Setter;

/**
 * Check JSR-303 features. Also check over @Service beans.
 * 
 */
@ExtendWith(SpringExtension.class)
public class ValidationTest extends AbstractBootTest {

	@Autowired
	private ValidationTestResource wineResource;

	@Autowired
	private ValidationResource validationResource;

	@Autowired
	private ValidatorBean validator;

	@Test
	public void testOk() {
		validator.validateCheck(newWine());
	}

	@Test
	public void testPerformance() {
		Wine newWine = newWine();
		Assertions.assertTimeout(Duration.ofSeconds(7), () -> {
			for (int i = 10000; i-- > 0;) {
				validator.validateCheck(newWine);
			}
		});
	}

	@Test
	public void testEmptyBean() {
		Assertions.assertThrows(ConstraintViolationException.class, () -> {
			validator.validateCheck(new Wine());
		});
	}

	@Test
	public void testHibernateExtension() {
		final Wine wine = newWine();
		wine.setYear(1);
		Assertions.assertThrows(ConstraintViolationException.class, () -> {
			validator.validateCheck(wine);
		});
	}

	@Test
	public void testValidateCheckArray() {
		validator.validateCheck(new Wine[] { newWine() });
	}

	@Test
	public void testValidateCheckCollection() {
		validator.validateCheck(Collections.singletonList(newWine()));
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	public void testUpperCase() {
		final Wine wine = newWine();
		wine.setName("c");
		final Set<ConstraintViolation<Wine>> validate = validator.validate(wine);
		Assertions.assertEquals(1, validate.size());
		final ConstraintViolation<?> constraintViolation = validate.iterator().next();
		Assertions.assertEquals(wine.getName(), constraintViolation.getInvalidValue());
		Assertions.assertEquals(wine, constraintViolation.getLeafBean());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.UpperCase.message", constraintViolation.getMessageTemplate());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.UpperCase.message", constraintViolation.getMessage());
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	public void testLowerCase() {
		final SampleBean wine = new SampleBean();
		wine.setLowerOnly("C");
		final Set<ConstraintViolation<SampleBean>> validate = validator.validate(wine);
		Assertions.assertEquals(1, validate.size());
		final ConstraintViolation<?> constraintViolation = validate.iterator().next();
		Assertions.assertEquals(wine.getLowerOnly(), constraintViolation.getInvalidValue());
		Assertions.assertEquals(wine, constraintViolation.getLeafBean());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.LowerCase.message", constraintViolation.getMessageTemplate());
		Assertions.assertEquals("org.ligoj.bootstrap.core.validation.LowerCase.message", constraintViolation.getMessage());
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	public void testLowerCase2() {
		final SampleBean wine = new SampleBean();
		wine.setLowerOnly(null);
		final Set<ConstraintViolation<SampleBean>> validate = validator.validate(wine);
		Assertions.assertTrue(validate.isEmpty());
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	public void testLowerCase3() {
		final SampleBean wine = new SampleBean();
		wine.setLowerOnly("c");
		final Set<ConstraintViolation<SampleBean>> validate = validator.validate(wine);
		Assertions.assertTrue(validate.isEmpty());
	}

	@Test
	public void testServiceOk() {
		wineResource.create(newWine());
	}

	@Test
	public void testServiceOk2() {
		wineResource.update(newWine());
	}

	/**
	 * Test a dummy bean.
	 */
	@Test
	public void testEmptyValidationDescription() throws ClassNotFoundException {
		final Map<String, List<String>> constraints = validationResource.describe(String.class.getName());
		Assertions.assertNotNull(constraints);
		Assertions.assertTrue(constraints.isEmpty());
	}

	/**
	 * Test a constrained bean.
	 */
	@Test
	public void testValidationDescription() throws ClassNotFoundException {
		final Map<String, List<String>> constraints = validationResource.describe(Wine.class.getName());
		Assertions.assertNotNull(constraints);
		Assertions.assertFalse(constraints.isEmpty());
		Assertions.assertEquals(1, constraints.get("picture").size());
		Assertions.assertEquals("org.hibernate.validator.constraints.Length", constraints.get("picture").get(0));
		Assertions.assertEquals(3, constraints.get("name").size());
		Assertions.assertEquals("org.hibernate.validator.constraints.Length", constraints.get("picture").get(0));
		Assertions.assertTrue(constraints.get("name").contains("javax.validation.constraints.NotEmpty"));
		Assertions.assertTrue(constraints.get("name").contains("org.ligoj.bootstrap.core.validation.UpperCase"));
	}

	private class SampleBean {

		@LowerCase
		@Getter
		@Setter
		private String lowerOnly;

	}

	/**
	 * Create a dummy, but filled wine.
	 */
	private Wine newWine() {
		final Wine wine = new Wine();
		wine.setCountry("C");
		wine.setDescription("C");
		wine.setGrapes("C");
		wine.setName("C");
		wine.setPicture("C");
		wine.setRegion("C");
		wine.setYear(2009);
		return wine;
	}
}
