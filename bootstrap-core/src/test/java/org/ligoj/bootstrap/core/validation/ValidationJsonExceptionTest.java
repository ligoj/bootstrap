/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
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
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
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
class ValidationJsonExceptionTest {

	@Test
	void validationJsonExceptionEmpty() {
		final var format = new InvalidFormatException(null, "", "", String.class);
		final var validationJsonException = new ValidationJsonException(format);
		Assertions.assertTrue(validationJsonException.getErrors().isEmpty());
	}

	@Test
	void validationJsonException() {
		final var format = new InvalidFormatException(null, "", "", String.class);
		format.prependPath(null, "property");
		format.prependPath("property", "property2");
		final var validationJsonException = new ValidationJsonException(format);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{property2.property=[{rule=String}]}", validationJsonException.getErrors().toString());
	}

	@Test
	void unrecognizedPropertyException() {
		final var format = new UnrecognizedPropertyException(null, "", null, String.class, "property",
				Collections.emptyList());
		format.prependPath(null, "property");
		format.prependPath("property", "property2");
		final var validationJsonException = new ValidationJsonException(format);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{property2.property=[{rule=Mapping}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	void validationJsonExceptionInteger() {
		final var format = new InvalidFormatException(null, "", "", int.class);
		format.prependPath(null, "property");
		format.prependPath("property", "property2");
		final var validationJsonException = new ValidationJsonException(format);
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
	void validationJsonExceptionCollection() {
		final var mapper = new ObjectMapper();
		final var e = Assertions.assertThrows(InvalidFormatException.class,
				() -> mapper.readValue("{\"items\":[{\"value\":\"A\"}]}", CollectionBean.class));
		final var validationJsonException = new ValidationJsonException(e);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{items[0].value=[{rule=Integer}]}", validationJsonException.getErrors().toString());
	}

	@Test
	void validationJsonExceptionCollectionNotFound() {
		final var collectionBean = new CollectionBean();
		final List<ItemBean> items = new ArrayList<>();
		items.add(new ItemBean());
		items.add(new ItemBean());
		collectionBean.setItems(items);

		final var format = new InvalidFormatException(null, "", "", int.class);
		format.prependPath("any", "id");
		format.prependPath(collectionBean, "items");
		final var validationJsonException = new ValidationJsonException(format);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{items.id=[{rule=Integer}]}", validationJsonException.getErrors().toString());
	}

	@Test
	void addError() {
		final var validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text}]}", validationJsonException.getErrors().toString());
	}

	@Test
	void addErrorRawArray() {
		final var validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text", "param1", "value1");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text, parameters={param1=value1}}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	void addErrorRawArray2() {
		final var validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text", "param1", "value1", "param2", "value2");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text, parameters={param1=value1, param2=value2}}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	void addErrorUnwrapArray() {
		final var validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text", new Serializable[] { new String[] { "param1", "value1" } });
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text, parameters={param1=value1}}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	void addErrorUnwrapArray2() {
		final var validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text",
				new Serializable[] { new String[] { "param1", "value1" }, new String[] { "param2", "value2" } });
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertTrue(validationJsonException.getErrors().toString()
				.startsWith("{p1=[{rule=text, parameters={[Ljava.lang.String;"));
	}

	@Test
	void addErrorUnwrapArrayIncomplete() {
		final var validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text", new Serializable[] { "param1" });
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text}]}", validationJsonException.getErrors().toString());
	}

	@Test
	void soloError() {
		final var validationJsonException = new ValidationJsonException("p1", "text");
		validationJsonException.addError("p1", "text");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text}]}", validationJsonException.getErrors().toString());
		Assertions.assertEquals("p1:text", validationJsonException.getMessage());
	}

	@Test
	void soloErrorParameter() {
		final var validationJsonException = new ValidationJsonException("p1", "text", "param1", "param2");
		validationJsonException.addError("p1", "text");
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text}]}", validationJsonException.getErrors().toString());
		Assertions.assertEquals("p1:text{param1,param2}", validationJsonException.getMessage());
	}

	@Test
	void addErrorWithParameters() {
		final var validationJsonException = new ValidationJsonException();
		validationJsonException.addError("p1", "text", "key", 5);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{p1=[{rule=text, parameters={key=5}}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	void assertNotNullError() {
		Assertions.assertThrows(ValidationJsonException.class, () -> ValidationJsonException.assertNotnull(null));
	}

	@Test
	void assertNotNull() {
		ValidationJsonException.assertNotnull("");
	}

	@Test
	void assertNullError() {
		Assertions.assertThrows(ValidationJsonException.class, () -> ValidationJsonException.assertNull("", "x", "y"));
	}

	@Test
	void assertNull() {
		ValidationJsonException.assertNull(null);
	}

	@Test
	void assertTrue() {
		ValidationJsonException.assertTrue(true, "x");
	}

	@Test
	void assertTrueError() {
		Assertions.assertEquals("{value=[{rule=y}]}", Assertions
				.assertThrows(ValidationJsonException.class, () -> ValidationJsonException.assertTrue(false, "y"))
				.getErrors().toString());
	}

	private <T extends Annotation> ConstraintAnnotationDescriptor<T> getAnnotation(final String fieldName,
			final Class<T> annotationClass) {
		return new ConstraintAnnotationDescriptor<>(
				FieldUtils.getField(SampleEntity.class, fieldName, true).getAnnotation(annotationClass));
	}

	@Test
	void assertTrueErrorObject() {
		final var object = new Object() {
			@Override
			public String toString() {
				return "NAME";
			}
		};
		Assertions.assertEquals("{value=[{rule=y, parameters={param1=value1, param2=NAME}}]}",
				Assertions.assertThrows(ValidationJsonException.class,
						() -> ValidationJsonException.assertTrue(false, "y", "param1", "value1", "param2", object))
						.getErrors().toString());
	}

	@Test
	void constraintViolationException() {
		final var bean = new SampleEntity();
		final Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();

		final var helper = ConstraintHelper.forAllBuiltinConstraints();

		final ConstraintDescriptor<NotEmpty> notEmptyNameDescriptor = new ConstraintDescriptorImpl<>(helper, null,
				getAnnotation("name", NotEmpty.class), ConstraintLocationKind.FIELD);
		final ConstraintDescriptor<NotEmpty> notEmptyGrapesDescriptor = new ConstraintDescriptorImpl<>(helper, null,
				getAnnotation("grapes", NotEmpty.class), ConstraintLocationKind.FIELD);
		final ConstraintDescriptor<Length> lengthNameDescriptor = new ConstraintDescriptorImpl<>(helper, null,
				getAnnotation("name", Length.class), ConstraintLocationKind.FIELD);
		violations.add(ConstraintViolationImpl.forBeanValidation("name-Empty", null, null, "interpolated",
				SampleEntity.class, bean, new Object(), "value", PathImpl.createPathFromString("name"),
				notEmptyNameDescriptor, ElementType.FIELD));
		violations.add(ConstraintViolationImpl.forBeanValidation("name-length", null, null, "interpolated",
				SampleEntity.class, bean, new Object(), "value", PathImpl.createPathFromString("name"),
				lengthNameDescriptor, ConstraintLocationKind.FIELD));
		violations.add(ConstraintViolationImpl.forBeanValidation("grapes-Empty", null, null, "interpolated",
				SampleEntity.class, bean, new Object(), "value", PathImpl.createPathFromString("grapes"),
				notEmptyGrapesDescriptor, ElementType.FIELD));

		final var violationException = Mockito.mock(ConstraintViolationException.class);
		Mockito.when(violationException.getConstraintViolations()).thenReturn(violations);

		final var validationJsonException = new ValidationJsonException(violationException);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals(
				"{name=[{rule=name-Empty}, {rule=name-length, parameters={min=0, max=50}}], grapes=[{rule=grapes-Empty}]}",
				validationJsonException.getErrors().toString());
	}

	@Test
	void constraintViolationExceptionParameter() {
		final var bean = new SampleEntity();
		final Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();

		final var helper = ConstraintHelper.forAllBuiltinConstraints();

		final ConstraintDescriptor<NotEmpty> notEmptyNameDescriptor = new ConstraintDescriptorImpl<>(helper, null,
				getAnnotation("name", NotEmpty.class), ConstraintLocationKind.FIELD);
		var path = PathImpl.createPathFromString("name");
		violations.add(ConstraintViolationImpl.forParameterValidation("name-Empty", null, null, "interpolated",
				SampleEntity.class, bean, new Object(), "value", path, notEmptyNameDescriptor, null,
				ElementType.PARAMETER));
		path.addParameterNode("parameter1", 0);

		final var violationException = Mockito.mock(ConstraintViolationException.class);
		Mockito.when(violationException.getConstraintViolations()).thenReturn(violations);

		final var validationJsonException = new ValidationJsonException(violationException);
		Assertions.assertFalse(validationJsonException.getErrors().isEmpty());
		Assertions.assertEquals("{parameter1=[{rule=name-Empty}]}", validationJsonException.getErrors().toString());
	}
}
