/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validate a rich text value provided by the user to ensure that it contains no malicious code, such as embedded
 * &lt;script&gt; elements.
 * <p>
 * Note that this constraint assumes you want to validate input which represents a body fragment of an HTML document. If
 * you instead want to validate input which represents a complete HTML document, add the {@code html}, {@code head} and
 * {@code body} tags to the used whitelist as required.
 *
 * @author George Gastaldi
 * @author Fabrice Daugan
 */
@Documented
@Constraint(validatedBy = SafeHtmlValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface SafeHtml {

	/**
	 * Default Key message.
	 * 
	 * @return Message key.
	 */
	String message() default "org.ligoj.bootstrap.core.validation.SafeHtml.message";

	/**
	 * JSR-303 requirement.
	 * 
	 * @return Empty groups.
	 */
	Class<?>[] groups() default {};

	/**
	 * JSR-303 requirement.
	 * 
	 * @return Empty payloads.
	 */
	Class<? extends Payload>[] payload() default {};

}
