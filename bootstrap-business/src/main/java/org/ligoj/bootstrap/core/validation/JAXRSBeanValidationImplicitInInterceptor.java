/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.logging.NoOpFaultListener;
import org.apache.cxf.message.Message;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Enforces correct parameters regarding JSR-303 and JSR-349. Eligible beans are
 * all JSR-349 annotated parameters plus non JAX-RS annotated objects.
 */
public class JAXRSBeanValidationImplicitInInterceptor extends JAXRSBeanValidationInInterceptor {

	/**
	 * Excluded parameter annotation name or package.
	 * {@link String#startsWith(String)} will be used.
	 */
	@Value("#{'${validation.excludes:javax.ws.rs,org.apache.cxf.jaxrs.ext.multipart.Multipart}'.split(',')}")
	private Collection<String> excludes = Collections.singleton(QueryParam.class.getPackage().getName());

	@Autowired
	private ValidatorBean validator;

	/**
	 * {@link NotNull} descriptor
	 */
	private static final ConstraintDescriptorImpl<NotNull> NOT_NULL_DESCRIPTOR = new ConstraintDescriptorImpl<>(new ConstraintHelper(),
			(Member) null, new ConstraintAnnotationDescriptor<NotNull>(new NotNull() {
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
					return new Class[0];
				}
			}), ElementType.PARAMETER);

	@Override
	protected void handleValidation(final Message message, final Object resourceInstance, final Method method,
			final List<Object> arguments) {
		super.handleValidation(message, resourceInstance, method, arguments);

		// Check each parameter
		final Set<ConstraintViolation<?>> validationErrors = new HashSet<>();
		for (int index = 0; index < arguments.size(); index++) {
			final Parameter parameter = method.getParameters()[index];
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
	 * @param bean
	 *            the bean to validate.
	 * @param parameter
	 *            the runtime annotations of this parameter.
	 * @param validationErrors
	 *            the errors list to fill.
	 */
	private void validate(final Object bean, final Method method, final Parameter parameter, final int index,
			final Set<ConstraintViolation<?>> validationErrors) {
		if (bean == null) {
			// Parameter is null, is it manually checked of managed by CXF for
			// multipart?
			// All non-body parameters are required by default
			final PathImpl propertyPath = PathImpl.createPathFromString(method.getName());
			propertyPath.addParameterNode(parameter.getName(), index);
			validationErrors.add(ConstraintViolationImpl.forParameterValidation(NotNull.class.getName(), null, null, "interpolated", null,
					null, null, null, propertyPath, NOT_NULL_DESCRIPTOR, null, null, null));
			return;
		}

		final Class<?> clazz = bean.getClass();
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
	 * @param annotation
	 *            the {@link Annotation} to check.
	 * @return <code>true</code> the given Annotation is eligible to bean
	 *         validation.
	 */
	private boolean hasToBeValidated(final Annotation annotation) {
		return excludes.stream().noneMatch(annotation.annotationType().getName()::startsWith);
	}

	/**
	 * Indicates all annotations of given {@link Parameter} are eligible to bean
	 * validation.
	 * 
	 * @param parameter
	 *            the parameter to check.
	 * @return <code>true</code> the given class is eligible to bean validation.
	 */
	private boolean hasToBeValidated(final Parameter parameter) {
		return Arrays.stream(parameter.getAnnotations()).allMatch(this::hasToBeValidated);
	}

}
