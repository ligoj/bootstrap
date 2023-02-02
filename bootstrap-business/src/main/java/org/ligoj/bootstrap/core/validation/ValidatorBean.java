/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Validation utility.
 * 
 * @author Fabrice Daugan
 * 
 */
@Component
public class ValidatorBean {

	/**
	 * Validator instance.
	 */
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * Validates all constraints on <code>object</code> and return a set of {@link ConstraintViolation}.
	 * 
	 * @param <T>    Bean type to validate.
	 * @param object object to validate
	 * @param groups group or list of groups targeted for validation.
	 * 
	 * @return constraint violations or an empty Set if none
	 */
	public <T> Set<ConstraintViolation<T>> validate(final T object, final Class<?>... groups) {
		return validator.validate(object, groups);
	}

	/**
	 * Validates all constraints on <code>objects</code> and return a set of {@link ConstraintViolation}.
	 * 
	 * @param <T>     Bean type to validate.
	 * @param objects objects to validate
	 * @param groups  group or list of groups targeted for validation.
	 * 
	 * @return constraint violations or an empty Set if none
	 */
	public <T> Set<ConstraintViolation<T>> validate(final Collection<T> objects, final Class<?>... groups) {
		final var errors = new HashSet<ConstraintViolation<T>>();
		// Validate the beans
		for (var object : objects) {
			errors.addAll(validate(object, groups));
		}
		return errors;
	}

	/**
	 * Validates all constraints on <code>object</code> and generate an exception containing {@link ConstraintViolation}
	 * objects when there is an error.
	 * 
	 * @param <T>    Bean type to validate.
	 * @param object object to validate
	 * @param groups group or list of groups targeted for validation.
	 */
	public <T> void validateCheck(final T object, final Class<?>... groups) {

		// Validate the bean and get the corresponding errors
		validateCheck(Collections.singleton(object), groups);
	}

	/**
	 * Validates all constraints on <code>objects</code> and generate an exception containing
	 * {@link ConstraintViolation} objects when there is an error.
	 * 
	 * @param <T>     Bean type to validate.
	 * @param objects object to validate
	 * @param groups  group or list of groups targeted for validation.
	 */
	public <T> void validateCheck(final T[] objects, final Class<?>... groups) {
		validateCheck(Arrays.asList(objects), groups);
	}

	/**
	 * Validates all constraints on <code>objects</code> and generate an exception containing
	 * {@link ConstraintViolation} objects when there is an error.
	 * 
	 * @param <T>     Bean type to validate.
	 * @param objects object to validate
	 * @param groups  group or list of groups targeted for validation.
	 */
	public <T> void validateCheck(final Collection<T> objects, final Class<?>... groups) {
		// Validate the beans
		final var errors = new HashSet<>(validate(objects, groups));
		if (!errors.isEmpty()) {
			// At least one error
			throw new ConstraintViolationException(StringUtils.join(errors, ','), errors);
		}
	}

	/**
	 * Return the {@link #validator} value.
	 * 
	 * @return the {@link #validator} value.
	 */
	public Validator getValidator() {
		return validator;
	}

}
