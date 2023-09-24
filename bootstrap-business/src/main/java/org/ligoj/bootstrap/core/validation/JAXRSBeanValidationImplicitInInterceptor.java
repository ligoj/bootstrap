/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.ws.rs.QueryParam;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.logging.NoOpFaultListener;
import org.apache.cxf.message.Message;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Enforces correct parameters regarding JSR-303 and JSR-349. Eligible beans are all JSR-349 annotated parameters plus
 * non JAX-RS annotated objects.
 */
public class JAXRSBeanValidationImplicitInInterceptor extends JAXRSBeanValidationInInterceptor {

	/**
	 * Excluded parameter annotation name or package. {@link String#startsWith(String)} will be used.
	 */
	@Value("#{'${validation.excludes:jakarta.ws.rs,org.apache.cxf.jaxrs.ext.multipart.Multipart}'.split(',')}")
	private final Collection<String> excludes = Collections.singleton(QueryParam.class.getPackage().getName());

	@Autowired
	private ValidatorBean validator;

	/**
	 * {@link NotNull} descriptor
	 */
	private static final ConstraintDescriptor<NotNull> NOT_NULL_DESCRIPTOR = new ConstraintDescriptorImpl<>(
			ConstraintHelper.forAllBuiltinConstraints(), null, new ConstraintAnnotationDescriptor<>(new NotNull() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return NotNull.class;
				}

				@Override
				public String message() {
					return "NotNull";
				}

				@Override
				public Class<?>[] groups() {
					return new Class<?>[0];
				}

				@SuppressWarnings("unchecked")
				@Override
				public Class<? extends Payload>[] payload() {
					return (Class<? extends Payload>[])new Class<?>[0];
				}
			}), ConstraintLocationKind.PARAMETER);

	@Override
	protected void handleValidation(final Message message, final Object resourceInstance, final Method method,
			final List<Object> arguments) {
		super.handleValidation(message, resourceInstance, method, arguments);

		// Check each parameter
		final Set<ConstraintViolation<?>> validationErrors = new HashSet<>();
		for (var index = 0; index < arguments.size(); index++) {
			final var parameter = method.getParameters()[index];
			if (hasToBeValidated(parameter)) {
				// This parameter is a not context, path or query parameter
				validate(arguments.get(index), method, parameter, index, validationErrors);
			}
		}

		// Check the veto
		if (!validationErrors.isEmpty()) {
			message.put(FaultListener.class.getName(), new NoOpFaultListener());
			throw new ConstraintViolationException(validationErrors);
		}
	}

	/**
	 * Validate a bean of given declared class.
	 *
	 * @param bean             the bean to validate.
	 * @param parameter        the runtime annotations of this parameter.
	 * @param validationErrors the errors list to fill.
	 */
	private void validate(final Object bean, final Method method, final Parameter parameter, final int index,
			final Set<ConstraintViolation<?>> validationErrors) {
		if (bean == null) {
			// Parameter is null, is it manually checked of managed by CXF for
			// multipart?
			// All non-body parameters are required by default
			final var propertyPath = PathImpl.createPathFromString(method.getName());
			propertyPath.addParameterNode(parameter.getName(), index);
			validationErrors.add(ConstraintViolationImpl.forParameterValidation(NotNull.class.getName(), null, null,
					"interpolated", null, null, null, null, propertyPath, NOT_NULL_DESCRIPTOR, null, null));
			return;
		}

		final var clazz = bean.getClass();
		if (Collection.class.isAssignableFrom(clazz)) {
			validate((Collection<?>) bean, validationErrors);
		} else if (clazz.isArray()) {
			validate((Object[]) bean, validationErrors);
		} else {
			validateSimpleBean(bean, validationErrors);
		}

	}

	/**
	 * Validate an array of beans.
	 */
	private void validate(final Object[] beans, final Set<ConstraintViolation<?>> validationErrors) {
		validationErrors.addAll(validator.validate(beans));
	}

	/**
	 * Validate a collection of beans.
	 */
	private void validate(final Collection<?> beans, final Set<ConstraintViolation<?>> validationErrors) {
		validationErrors.addAll(validator.validate(beans));
	}

	/**
	 * Validate a simple beans.
	 */
	private void validateSimpleBean(final Object bean, final Set<ConstraintViolation<?>> validationErrors) {
		validationErrors.addAll(validator.validate(bean));
	}

	/**
	 * Indicates the given annotation is eligible to bean validation.
	 *
	 * @param annotation the {@link Annotation} to check.
	 * @return <code>true</code> the given Annotation is eligible to bean validation.
	 */
	private boolean hasToBeValidated(final Annotation annotation) {
		return excludes.stream().noneMatch(annotation.annotationType().getName()::startsWith);
	}

	/**
	 * Indicates all annotations of given {@link Parameter} are eligible to bean validation.
	 *
	 * @param parameter the parameter to check.
	 * @return <code>true</code> the given class is eligible to bean validation.
	 */
	private boolean hasToBeValidated(final Parameter parameter) {
		return Arrays.stream(parameter.getAnnotations()).allMatch(this::hasToBeValidated);
	}

}
