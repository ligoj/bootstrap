/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotEmpty;
import javax.validation.metadata.ConstraintDescriptor;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import lombok.Getter;
import lombok.Setter;

/**
 * Check some validation serialization features of {@link ValidationJsonException}.
 */
public class ValidationJsonExceptionTest {

	@Test
	public void testValidationJsonExceptionEmpty() {
		final InvalidFormatException format = new InvalidFormatException(null, "", "", String.class);
		final ValidationJsonException validationJsonException = new ValidationJsonException(format);
		Assertions.assertTrue(validationJsonException.getErrors().isEmpty());
	}

	@Test
	public void testValidationJsonException() {
		final InvalidFormatException format = new InvalidFormatException(null, "", "", String.class);
		format.prependPath(null, "property");
		format.prependPath("property", "property2");
		final ValidationJsonException validationJsonException = new ValidationJsonException(format);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{property2.property=[{rule=String}]}", validationJsonException.getErrors().toString());
	}

	@Test
	public void testUnrecognizedPropertyException() {
		final UnrecognizedPropertyException format = new UnrecognizedPropertyException(null, "", null, String.class,
				"property", Collections.emptyList());
		format.prependPath(null, "property");
		format.prependPath("property", "property2");
		final ValidationJsonException validationJsonException = new ValidationJsonException(format);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{property2.property=[{rule=Mapping}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	public void testValidationJsonExceptionInteger() {
		final InvalidFormatException format = new InvalidFormatException(null, "", "", int.class);
		format.prependPath(null, "property");
		format.prependPath("property", "property2");
		final ValidationJsonException validationJsonException = new ValidationJsonException(format);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{property2.property=[{rule=Integer}]}",
				validationJsonException.getErrors().toString());
	}

	@Getter
	@Setter
	private static class ItemBean {
		private int value;
	}

	@Getter
	@Setter
	private static class CollectionBean {
		private List<ItemBean> items;
	}

	@Test
	public void testValidationJsonExceptionCollection() {
		final ObjectMapper mapper = new ObjectMapper();
		final InvalidFormatException e = Assertions.assertThrows(InvalidFormatException.class,
				() -> mapper.readValue("{\"items\":[{\"value\":\"A\"}]}", CollectionBean.class));
		final ValidationJsonException validationJsonException = new ValidationJsonException(e);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{items[0].value=[{rule=Integer}]}", validationJsonException.getErrors().toString());
	}

	@Test
	public void testValidationJsonExceptionCollectionNotFound() {
		final CollectionBean collectionBean = new CollectionBean();
		final List<ItemBean> items = new ArrayList<>();
		items.add(new ItemBean());
		items.add(new ItemBean());
		collectionBean.setItems(items);

		final InvalidFormatException format = new InvalidFormatException(null, "", "", int.class);
		format.prependPath("any", "id");
		format.prependPath(collectionBean, "items");
		final ValidationJsonException validationJsonException = new ValidationJsonException(format);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{items.id=[{rule=Integer}]}", validationJsonException.getErrors().toString());
	}

	@Test
	public void testAddError() {
		final ValidationJsonException validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text}]}", validationJsonException.getErrors().toString());
	}

	@Test
	public void testSoloError() {
		final ValidationJsonException validationJsonException = new ValidationJsonException("p1", "text");
		validationJsonException.addError("p1", "text");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text}]}", validationJsonException.getErrors().toString());
		Assertions.assertEquals("p1:text", validationJsonException.getMessage());
	}

	@Test
	public void testSoloErrorParameter() {
		final ValidationJsonException validationJsonException = new ValidationJsonException("p1", "text", "param1",
				"param2");
		validationJsonException.addError("p1", "text");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text}]}", validationJsonException.getErrors().toString());
		Assertions.assertEquals("p1:text{param1,param2}", validationJsonException.getMessage());
	}

	@Test
	public void testAddErrorWithParameters() {
		final ValidationJsonException validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text", "key", 5);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text, parameters={key=5}}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	public void testAssertNotNullError() {
		Assertions.assertThrows(ValidationJsonException.class, () -> {
			ValidationJsonException.assertNotnull(null);
		});
	}

	@Test
	public void testAssertNotNull() {
		ValidationJsonException.assertNotnull("");
	}

	@Test
	public void testAsserNullError() {
		Assertions.assertThrows(ValidationJsonException.class, () -> {
			ValidationJsonException.assertNull("", "x", "y");
		});
	}

	@Test
	public void testAssertNull() {
		ValidationJsonException.assertNull(null);
	}

	@Test
	public void testAssertTrue() {
		ValidationJsonException.assertTrue(true, "x");
	}

	@Test
	public void testAssertTrueError() {
		Assertions.assertEquals("{value=[{rule=y}]}", Assertions.assertThrows(ValidationJsonException.class, () -> {
			ValidationJsonException.assertTrue(false, "y");
		}).getErrors().toString());
	}

	private <T extends Annotation> ConstraintAnnotationDescriptor<T> getAnnotation(final String fieldName,
			final Class<T> annotationClass) {
		return new ConstraintAnnotationDescriptor<>(
				FieldUtils.getField(SampleEntity.class, fieldName, true).getAnnotation(annotationClass));
	}

	@Test
	public void testConstraintViolationException() {
		final SampleEntity bean = new SampleEntity();
		final Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();

		final ConstraintHelper helper = new ConstraintHelper();

		final ConstraintDescriptor<NotEmpty> notEmptyNameDescriptor = new ConstraintDescriptorImpl<>(helper,
				(Member) null, getAnnotation("name", NotEmpty.class), ElementType.FIELD);
		final ConstraintDescriptor<NotEmpty> notEmptyGrapesDescriptor = new ConstraintDescriptorImpl<>(helper,
				(Member) null, getAnnotation("grapes", NotEmpty.class), ElementType.FIELD);
		final ConstraintDescriptor<Length> lengthNameDescriptor = new ConstraintDescriptorImpl<>(helper, (Member) null,
				getAnnotation("name", Length.class), ElementType.FIELD);
		violations.add(ConstraintViolationImpl.<SampleEntity>forBeanValidation("name-Empty", null, null, "interpolated",
				SampleEntity.class, bean, new Object(), "value", PathImpl.createPathFromString("name"),
				notEmptyNameDescriptor, ElementType.FIELD, null));
		violations.add(ConstraintViolationImpl.<SampleEntity>forBeanValidation("name-length", null, null,
				"interpolated", SampleEntity.class, bean, new Object(), "value", PathImpl.createPathFromString("name"),
				lengthNameDescriptor, ElementType.FIELD, null));
		violations.add(ConstraintViolationImpl.<SampleEntity>forBeanValidation("grapes-Empty", null, null,
				"interpolated", SampleEntity.class, bean, new Object(), "value",
				PathImpl.createPathFromString("grapes"), notEmptyGrapesDescriptor, ElementType.FIELD, null));

		final ConstraintViolationException violationException = Mockito.mock(ConstraintViolationException.class);
		Mockito.when(violationException.getConstraintViolations()).thenReturn(violations);

		final ValidationJsonException validationJsonException = new ValidationJsonException(violationException);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals(
				"{name=[{rule=name-Empty}, {rule=name-length, parameters={min=0, max=50}}], grapes=[{rule=grapes-Empty}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	public void testConstraintViolationExceptionParameter() {
		final SampleEntity bean = new SampleEntity();
		final Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();

		final ConstraintHelper helper = new ConstraintHelper();

		final ConstraintDescriptor<NotEmpty> notEmptyNameDescriptor = new ConstraintDescriptorImpl<>(helper,
				(Member) null, getAnnotation("name", NotEmpty.class), ElementType.FIELD);
		PathImpl path = PathImpl.createPathFromString("name");
		violations.add(ConstraintViolationImpl.<SampleEntity>forParameterValidation("name-Empty", null, null,
				"interpolated", SampleEntity.class, bean, new Object(), "value", path, notEmptyNameDescriptor,
				ElementType.PARAMETER, null, null));
		path.addParameterNode("parameter1", 0);

		final ConstraintViolationException violationException = Mockito.mock(ConstraintViolationException.class);
		Mockito.when(violationException.getConstraintViolations()).thenReturn(violations);

		final ValidationJsonException validationJsonException = new ValidationJsonException(violationException);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{parameter1=[{rule=name-Empty}]}", validationJsonException.getErrors().toString());
	}
}