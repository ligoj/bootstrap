/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

/**
 * Char based validator.
 * 
 * @param <A> Annotation type.
 */
public abstract class AbstractCharValidator<A extends Annotation> implements ConstraintValidator<A, String> {

	@Override
	public void initialize(final A upperCase) {
		// Nothing to initialize
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		return StringUtils.isEmpty(value) || isValid(value);
	}

	private boolean isValid(final String value) {
		for (var i = 0; i < value.length(); i++) {
			if (!isValidChar(value.charAt(i))) {
				// This char is Upper
				return false;
			}
		}
		// No char with upper case
		return true;
	}

	/**
	 * Indicates the specified char is valid or not.
	 *
	 * @param c The char to validate.
	 * @return <code>true</code> when the specified char is valid.
	 */
	protected abstract boolean isValidChar(char c);
}