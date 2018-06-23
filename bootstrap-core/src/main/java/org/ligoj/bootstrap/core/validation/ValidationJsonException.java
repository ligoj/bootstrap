/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import lombok.Getter;

/**
 * An exception containing validation errors into a {@link Map}.
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
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
	@Getter
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
	 * @param message
	 *            the raw validation error message.
	 */
	public ValidationJsonException(final String message) {
		super(message);
	}

	/**
	 * Constructor from mapping exception : invalid types.
	 * 
	 * @param mappingException
	 *            validation exception containing errors.
	 */
	public ValidationJsonException(final InvalidFormatException mappingException) {
		this(mappingException, String.valueOf(mappingException.getValue()), parseRule(mappingException));
	}

	/**
	 * Constructor from errors.
	 * 
	 * @param validation
	 *            validation exception containing errors.
	 */
	public ValidationJsonException(final ConstraintViolationException validation) {
		this(validation.getMessage());
		validation.getConstraintViolations()
				.forEach(e -> errors.computeIfAbsent(getPropertyPath(e), k -> new ArrayList<>()).add(serializeHibernateValidationError(e)));
	}

	/**
	 * Constructor from mapping exception : invalid property.
	 * 
	 * @param mappingException
	 *            validation exception containing errors.
	 */
	public ValidationJsonException(final UnrecognizedPropertyException mappingException) {
		this(mappingException, mappingException.getPropertyName(), "Mapping");
	}

	private ValidationJsonException(final JsonMappingException mappingException, final String message, final String rule) {
		this(message);
		final StringBuilder propertyPath = buildPropertyPath(mappingException.getPath());
		if (propertyPath.length() > 0) {

			// Add the error.
			final Map<String, Serializable> error = new HashMap<>();
			error.put("rule", rule);
			errors.put(propertyPath.toString(), Collections.singletonList(error));
		}
	}

	/**
	 * Constructor for a single error.
	 * 
	 * @param propertyName
	 *            Name of the JSon property
	 * @param errorText
	 *            I18N key of the message.
	 * @param parametersKeyValue
	 *            Optional key and value pairs.
	 */
	public ValidationJsonException(final String propertyName, final Serializable errorText, final Serializable... parametersKeyValue) {
		this(propertyName + ":" + errorText + (parametersKeyValue.length > 0 ? ArrayUtils.toString(parametersKeyValue) : ""));
		addError(propertyName, errorText, parametersKeyValue);
	}

	/**
	 * Helper method to add an error on property with a single message error
	 * 
	 * @param propertyName
	 *            Name of the JSon property
	 * @param errorText
	 *            I18N key of the message.
	 * @param parametersKeyValue
	 *            optional parameter, key and value pairs.
	 */
	public void addError(final String propertyName, final Serializable errorText, final Serializable... parametersKeyValue) {
		final Map<String, Serializable> error = new HashMap<>();
		error.put("rule", errorText);
		errors.put(propertyName, Collections.singletonList(error));

		// Add parameters
		if (parametersKeyValue.length > 1) {
			error.put("parameters", (Serializable) toMap(parametersKeyValue));
		}
	}

	/**
	 * Transform a K,V values list to a Map.
	 */
	private Map<String, Serializable> toMap(final Serializable... parametersKeyValue) {
		final Map<String, Serializable> parameters = new HashMap<>();
		for (int i = 0; i < parametersKeyValue.length; i += 2) {
			parameters.put(parametersKeyValue[i].toString(), parametersKeyValue[i + 1]);
		}
		return parameters;
	}

	/**
	 * Build and return a property path of given exception.
	 */
	private StringBuilder buildPropertyPath(final List<InvalidFormatException.Reference> path) {
		final StringBuilder propertyPath = new StringBuilder();
		InvalidFormatException.Reference parent = null;
		for (final InvalidFormatException.Reference reference : path) {
			buildPropertyPath(propertyPath, reference, parent);
			parent = reference;
		}
		return propertyPath;
	}

	/**
	 * Build and return a property path of given exception.
	 */
	private void buildPropertyPath(final StringBuilder propertyPath, final InvalidFormatException.Reference reference,
			final InvalidFormatException.Reference parent) {
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
	private void buildNestedPropertyPath(final StringBuilder propertyPath, final InvalidFormatException.Reference reference) {
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
	private static String parseRule(final InvalidFormatException mappingException) {
		final String rule = StringUtils.capitalize(mappingException.getTargetType().getSimpleName());

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
	 * @param error
	 *            validation error
	 * @return serialized error
	 */
	private Map<String, Serializable> serializeHibernateValidationError(final ConstraintViolation<?> violation) {
		final Map<String, Serializable> error = new HashMap<>();
		final Map<String, Serializable> parameters = new HashMap<>();
		for (final Map.Entry<String, Object> entry : violation.getConstraintDescriptor().getAttributes().entrySet()) {
			// ignore some parameters
			if (!IGNORED_PARAMETERS.contains(entry.getKey())) {
				parameters.put(entry.getKey(), (Serializable) entry.getValue());
				error.put("parameters", (Serializable) parameters);
			}
		}
		error.put("rule", ClassUtils.getShortClassName(
				StringUtils.removeEnd(StringUtils.removeEnd(StringUtils.removeStart(violation.getMessageTemplate(), "{"), "}"), ".message")));
		return error;
	}

	/**
	 * Check the value is null for given fields.
	 * 
	 * @param value
	 *            Is the nullable value to check.
	 * @param fields
	 *            Are the fields to report as error. When zero-size, {@value #DEFAULT_FIELD} field name is used.
	 */
	public static void assertNull(final Object value, final String... fields) {
		if (value != null) {
			throw newValidationJsonException(Null.class.getSimpleName(), fields);
		}
	}

	/**
	 * Check the value is not null for given fields.
	 * 
	 * @param value
	 *            Is the nullable value to check.
	 * @param fields
	 *            Are the fields to report as error. When zero-size, "value" field name is used.
	 */
	public static void assertNotnull(final Object value, final String... fields) {
		if (value == null) {
			throw newValidationJsonException(NotNull.class.getSimpleName(), fields);
		}
	}

	/**
	 * Build a validation exception.
	 * 
	 * @param error
	 *            the error code to throw.
	 * @param fields
	 *            Are the fields to report as error.When zero-size, {@value #DEFAULT_FIELD} field name is used.
	 * @return a validation exception containing given errors.
	 */
	public static ValidationJsonException newValidationJsonException(final String error, final String... fields) {
		final ValidationJsonException exception = new ValidationJsonException(error + ":" + ArrayUtils.toString(fields));
		if (fields.length == 0) {
			exception.addError(DEFAULT_FIELD, error);
		} else {
			for (final String field : fields) {
				exception.addError(field, error);
			}
		}
		return exception;
	}

	/**
	 * Throw a validation error when the given value is false.
	 * 
	 * @param assertTrue
	 *            the boolean to check.
	 * @param error
	 *            the error code to throw.
	 * @param params
	 *            the optional parameter of error.
	 */
	public static void assertTrue(final boolean assertTrue, final String error, final Object... params) {
		if (!assertTrue) {
			final ValidationJsonException exception = new ValidationJsonException();
			exception.addError(DEFAULT_FIELD, error, params);
			throw exception;
		}
	}
}