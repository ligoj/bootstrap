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
 * Note that this constraint validates input which represents a body fragment of an HTML document, against the fixed
 * JSoup <code>Safelist.relaxed()</code> safelist, also accepting <code>#</code> links. The safelist is not
 * configurable, so a complete HTML document (with {@code html}, {@code head} and {@code body} tags) is rejected.
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
