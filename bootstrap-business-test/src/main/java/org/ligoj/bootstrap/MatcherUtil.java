/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.ligoj.bootstrap.core.validation.ValidationJsonException;

/**
 * {@link ConstraintViolationException} and {@link ValidationJsonException}
 * utilities
 */
public class MatcherUtil {

	/**
	 * Check the exception for a property and a specific associated message.
	 * 
	 * @param ex
	 *            The exception to check.
	 * @param field
	 *            The error property name.
	 * @param message
	 *            The unique error message
	 */
	public static void assertThrows(final ValidationJsonException ex, final String field, final String message) {
		final var errors = CollectionUtils.emptyIfNull(ex.getErrors().get(field));
		Assertions.assertEquals(errors.isEmpty() ? field : message,
				errors.stream().map(e -> e.get("rule")).filter(message::equals).findAny()
						.orElseGet(() -> errors.isEmpty() ? ex.getErrors().keySet().toString()
								: errors.stream().findFirst().map(e -> e.get("rule")).orElse(null)));
	}

	/**
	 * Check the exception for a property and a specific associated message.
	 * 
	 * @param ex
	 *            The exception to check.
	 * @param field
	 *            The error property name.
	 * @param message
	 *            The unique error message
	 */
	public static void assertThrows(final ConstraintViolationException ex, final String field, final String message) {
		final var errors = ex.getConstraintViolations().stream()
				.filter(v -> field.equals(v.getPropertyPath().toString())).collect(Collectors.toList());
		final List<String> errorsS = new ArrayList<>();
		errors.forEach(v -> {
			errorsS.add(StringUtils.defaultIfBlank(ClassUtils.getShortClassName(ClassUtils.getPackageName(v.getMessageTemplate())), null));
			errorsS.add(StringUtils.defaultIfBlank(v.getMessageTemplate(), null));
		});
		final var errorsS2 = errorsS.stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toList());
		Assertions.assertEquals(errors.isEmpty() ? field : message.toLowerCase(),
				errorsS2.stream().filter(message::equalsIgnoreCase).findAny().orElseGet(
						() -> errors.isEmpty()
								? ex.getConstraintViolations().stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet())
										.toString()
								: errorsS2.stream().findFirst().orElse(null)));
	}

}
