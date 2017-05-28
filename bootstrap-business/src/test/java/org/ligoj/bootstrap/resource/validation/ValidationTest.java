package org.ligoj.bootstrap.resource.validation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.core.validation.LowerCase;
import org.ligoj.bootstrap.core.validation.ValidatorBean;
import org.ligoj.bootstrap.core.validation.Wine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.Getter;
import lombok.Setter;

/**
 * Check JSR-303 features. Also check over @Service beans.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
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

	@Test(timeout = 7000)
	public void testPerformance() {
		Wine newWine = newWine();
		for (int i = 10000; i-- > 0;) {
			validator.validateCheck(newWine);
		}
	}

	@Test(expected = ConstraintViolationException.class)
	public void testEmptyBean() {
		validator.validateCheck(new Wine());
	}

	@Test(expected = ConstraintViolationException.class)
	public void testHibernateExtension() {
		final Wine wine = newWine();
		wine.setYear(1);
		validator.validateCheck(wine);
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
		Assert.assertEquals(1, validate.size());
		final ConstraintViolation<?> constraintViolation = validate.iterator().next();
		Assert.assertEquals(wine.getName(), constraintViolation.getInvalidValue());
		Assert.assertEquals(wine, constraintViolation.getLeafBean());
		Assert.assertEquals("org.ligoj.bootstrap.core.validation.UpperCase.message", constraintViolation.getMessageTemplate());
		Assert.assertEquals("org.ligoj.bootstrap.core.validation.UpperCase.message", constraintViolation.getMessage());
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	public void testLowerCase() {
		final SampleBean wine = new SampleBean();
		wine.setLowerOnly("C");
		final Set<ConstraintViolation<SampleBean>> validate = validator.validate(wine);
		Assert.assertEquals(1, validate.size());
		final ConstraintViolation<?> constraintViolation = validate.iterator().next();
		Assert.assertEquals(wine.getLowerOnly(), constraintViolation.getInvalidValue());
		Assert.assertEquals(wine, constraintViolation.getLeafBean());
		Assert.assertEquals("org.ligoj.bootstrap.core.validation.LowerCase.message", constraintViolation.getMessageTemplate());
		Assert.assertEquals("org.ligoj.bootstrap.core.validation.LowerCase.message", constraintViolation.getMessage());
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	public void testLowerCase2() {
		final SampleBean wine = new SampleBean();
		wine.setLowerOnly(null);
		final Set<ConstraintViolation<SampleBean>> validate = validator.validate(wine);
		Assert.assertTrue(validate.isEmpty());
	}

	/**
	 * Check the upper case constraint feature.
	 */
	@Test
	public void testLowerCase3() {
		final SampleBean wine = new SampleBean();
		wine.setLowerOnly("c");
		final Set<ConstraintViolation<SampleBean>> validate = validator.validate(wine);
		Assert.assertTrue(validate.isEmpty());
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
		Assert.assertNotNull(constraints);
		Assert.assertTrue(constraints.isEmpty());
	}

	/**
	 * Test a constrained bean.
	 */
	@Test
	public void testValidationDescription() throws ClassNotFoundException {
		final Map<String, List<String>> constraints = validationResource.describe(Wine.class.getName());
		Assert.assertNotNull(constraints);
		Assert.assertFalse(constraints.isEmpty());
		Assert.assertEquals(1, constraints.get("picture").size());
		Assert.assertEquals("org.hibernate.validator.constraints.Length", constraints.get("picture").get(0));
		Assert.assertEquals(3, constraints.get("name").size());
		Assert.assertEquals("org.hibernate.validator.constraints.Length", constraints.get("picture").get(0));
		Assert.assertTrue(constraints.get("name").contains("org.hibernate.validator.constraints.NotEmpty"));
		Assert.assertTrue(constraints.get("name").contains("org.ligoj.bootstrap.core.validation.UpperCase"));
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
