/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Lower case constraint.
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LowerCaseValidator.class)
public @interface LowerCase {

	/**
	 * Default Key message.
	 * 
	 * @return Message key.
	 */
	String message() default "org.ligoj.bootstrap.core.validation.LowerCase.message";

	/**
	 * JSR-303 requirement.
	 * 
	 * @return Empty groups.
	 */
	Class<?>[] groups() default {

	};

	/**
	 * JSR-303 requirement.
	 * 
	 * @return Empty payloads.
	 */
	Class<? extends Payload>[] payload() default {

	};
}