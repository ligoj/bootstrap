/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.hibernate.validator.internal.engine.path.PathImpl;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * An exception containing validation errors into a {@link Map}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
@Getter
public class ValidationJsonException extends RuntimeException {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default field name.
	 */
	public static final String DEFAULT_FIELD = "value";

	/**
	 * Ignored parameters for Hibernate validation serialization.
	 */
	private static final Set<String> IGNORED_PARAMETERS = new HashSet<>();

	static {
		IGNORED_PARAMETERS.add("message");
		IGNORED_PARAMETERS.add("groups");
		IGNORED_PARAMETERS.add("payload");
	}

	/**
	 * JSR-303/349 errors, Key is the property name. Values is the associated translated message errors.
	 */
	@JsonProperty
	private final Map<String, List<Map<String, Serializable>>> errors = new LinkedHashMap<>();

	/**
	 * JSon constructor.
	 */
	public ValidationJsonException() {
		// Default constructor
	}

	/**
	 * Constructor with message context.
	 *
	 * @param message the raw validation error message.
	 */
	public ValidationJsonException(final String message) {
		super(message);
	}

	/**
	 * Constructor from mapping exception : invalid type conversion.
	 *
	 * @param mappingException validation exception containing errors.
	 */
	public ValidationJsonException(final MismatchedInputException mappingException) {
		this(mappingException, String.valueOf(mappingException.getMessage()), parseRule(mappingException));
	}

	/**
	 * Constructor from errors.
	 *
	 * @param validation validation exception containing errors.
	 */
	public ValidationJsonException(final ConstraintViolationException validation) {
		this(validation.getMessage());
		validation.getConstraintViolations().forEach(e -> errors
				.computeIfAbsent(getPropertyPath(e), k -> new ArrayList<>()).add(serializeHibernateValidationError(e)));
	}

	/**
	 * Constructor from mapping exception : invalid property.
	 *
	 * @param mappingException validation exception containing errors.
	 */
	public ValidationJsonException(final UnrecognizedPropertyException mappingException) {
		this(mappingException, mappingException.getPropertyName(), "Mapping");
	}

	private ValidationJsonException(final JsonMappingException mappingException, final String message,
			final String rule) {
		this(message);
		final var propertyPath = buildPropertyPath(mappingException.getPath());
		if (!propertyPath.isEmpty()) {

			// Add the error.
			final var error = new HashMap<String, Serializable>();
			error.put("rule", rule);
			errors.put(propertyPath.toString(), Collections.singletonList(error));
		}
	}

	/**
	 * Constructor for a single error.
	 *
	 * @param propertyName       Name of the JSon property
	 * @param errorText          I18N key of the message.
	 * @param parametersKeyValue Optional key and value pairs.
	 */
	public ValidationJsonException(final String propertyName, final Serializable errorText,
			final Serializable... parametersKeyValue) {
		this(propertyName + ":" + errorText
				+ (parametersKeyValue.length > 0 ? ArrayUtils.toString(parametersKeyValue) : ""));
		addError(propertyName, errorText, parametersKeyValue);
	}

	/**
	 * Helper method to add an error on property with a single message error
	 *
	 * @param propertyName       Name of the JSon property
	 * @param errorText          I18N key of the message.
	 * @param parametersKeyValue optional parameter, key and value pairs.
	 */
	public void addError(final String propertyName, final Serializable errorText,
			final Serializable... parametersKeyValue) {
		final var error = new HashMap<String, Serializable>();
		error.put("rule", errorText);
		errors.put(propertyName, Collections.singletonList(error));

		final Serializable[] params;
		if (parametersKeyValue.length == 1 && parametersKeyValue[0].getClass().isArray()
				&& Array.getLength(parametersKeyValue[0]) > 0) {
			params = toArray((Object[]) parametersKeyValue[0]);
		} else {
			params = parametersKeyValue;
		}

		// Add parameters
		if (params.length > 1) {
			error.put("parameters", (Serializable) toMap(params));
		}
	}

	private Serializable[] toArray(final Object[] raw) {
		final var result = new Serializable[raw.length];
		for (var i = 0; i < raw.length; i++) {
			final var obj = raw[i];
			if (obj instanceof Serializable ser) {
				result[i] = ser;
			} else {
				result[i] = obj.toString();
			}
		}
		return result;
	}

	/**
	 * Transform a K,V values list to a Map.
	 */
	private Map<String, Serializable> toMap(final Serializable... params) {
		final var parameters = new HashMap<String, Serializable>();
		for (var i = 0; i < params.length; i += 2) {
			parameters.put(params[i].toString(), params[i + 1]);
		}
		return parameters;
	}

	/**
	 * Build and return a property path of given exception.
	 */
	private StringBuilder buildPropertyPath(final List<JsonMappingException.Reference> path) {
		final var propertyPath = new StringBuilder();
		JsonMappingException.Reference parent = null;
		for (final var reference : path) {
			buildPropertyPath(propertyPath, reference, parent);
			parent = reference;
		}
		return propertyPath;
	}

	/**
	 * Build and return a property path of given exception.
	 */
	private void buildPropertyPath(final StringBuilder propertyPath, final JsonMappingException.Reference reference,
			final JsonMappingException.Reference parent) {
		if (parent != null) {
			buildNestedPropertyPath(propertyPath, reference);
		}
		if (reference.getFieldName() != null) {
			propertyPath.append(reference.getFieldName());
		}
	}

	/**
	 * Build nested property path.
	 */
	private void buildNestedPropertyPath(final StringBuilder propertyPath,
			final JsonMappingException.Reference reference) {
		if (reference.getIndex() > -1) {
			propertyPath.append('[');
			propertyPath.append(reference.getIndex());
			propertyPath.append(']');
		} else {
			propertyPath.append('.');
		}
	}

	/**
	 * Parse the rule name from the Jackson mapping exception message in the given violation.
	 */
	private static String parseRule(final MismatchedInputException mappingException) {
		final var rule = StringUtils.capitalize(mappingException.getTargetType().getSimpleName());

		// Manage the primitive type "int" due to Jackson 2.x new features
		return "Int".equals(rule) ? "Integer" : rule;
	}

	/**
	 * Return the property path from the given validation error.
	 */
	private String getPropertyPath(final ConstraintViolation<?> error) {
		if (((PathImpl) error.getPropertyPath()).getLeafNode().getKind() == ElementKind.PARAMETER) {
			// JSR-349 - Parameter, drop parent context
			return ((PathImpl) error.getPropertyPath()).getLeafNode().getName();
		}
		// JSR-303 - Bean violation
		return error.getPropertyPath().toString();
	}

	/**
	 * serialize a violation from Hibernate validation
	 *
	 * @param violation The validation error
	 * @return serialized error
	 */
	private Map<String, Serializable> serializeHibernateValidationError(final ConstraintViolation<?> violation) {
		final Map<String, Serializable> error = new HashMap<>();
		final Map<String, Serializable> parameters = new HashMap<>();
		for (final var entry : violation.getConstraintDescriptor().getAttributes().entrySet()) {
			// ignore some parameters
			if (!IGNORED_PARAMETERS.contains(entry.getKey())) {
				parameters.put(entry.getKey(), (Serializable) entry.getValue());
				error.put("parameters", (Serializable) parameters);
			}
		}
		error.put("rule",
				ClassUtils.getShortClassName(Strings.CS.removeEnd(
						Strings.CS.removeEnd(Strings.CS.removeStart(violation.getMessageTemplate(), "{"), "}"),
						".message")));
		return error;
	}

	/**
	 * Check the value is null for given fields.
	 *
	 * @param value  Is the nullable value to check.
	 * @param fields Are the fields to report as error. When zero-size, {@value #DEFAULT_FIELD} field name is used.
	 */
	public static void assertNull(final Object value, final String... fields) {
		if (value != null) {
			throw newValidationJsonException(Null.class.getSimpleName(), fields);
		}
	}

	/**
	 * Check the value is not null for given fields.
	 *
	 * @param value  Is the nullable value to check.
	 * @param fields Are the fields to report as error. When zero-size, "value" field name is used.
	 */
	public static void assertNotnull(final Object value, final String... fields) {
		if (value == null) {
			throw newValidationJsonException(NotNull.class.getSimpleName(), fields);
		}
	}

	/**
	 * Build a validation exception.
	 *
	 * @param error  the error code to throw.
	 * @param fields Are the fields to report as error.When zero-size, {@value #DEFAULT_FIELD} field name is used.
	 * @return a validation exception containing given errors.
	 */
	public static ValidationJsonException newValidationJsonException(final String error, final String... fields) {
		final var exception = new ValidationJsonException(error + ":" + ArrayUtils.toString(fields));
		if (fields.length == 0) {
			exception.addError(DEFAULT_FIELD, error);
		} else {
			for (final var field : fields) {
				exception.addError(field, error);
			}
		}
		return exception;
	}

	/**
	 * Throw a validation error when the given value is false.
	 *
	 * @param assertTrue the boolean to check.
	 * @param error      the error code to throw.
	 * @param params     the optional parameter of error.
	 */
	public static void assertTrue(final boolean assertTrue, final String error, final Object... params) {
		if (!assertTrue) {
			final var exception = new ValidationJsonException();
			exception.addError(DEFAULT_FIELD, error, params);
			throw exception;
		}
	}
}