/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.Nullable;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ClassUtils;

/**
 * {@link JAXRSBeanValidationImplicitInInterceptor} checks.
 */
@ExtendWith(SpringExtension.class)
class JAXRSBeanValidationImplicitInInterceptorTest extends AbstractBootTest {

	@Autowired
	private JAXRSBeanValidationImplicitInInterceptor validationInInterceptor;

	static class TestClass {
		void empty() {
			//
		}

		/**
		 * @param param Simple String.
		 */
		public void simple(final String param) {
			//
		}

		/**
		 * @param param Named query parameter.
		 */
		public void queryObject(final @QueryParam("some") Object param) {
			//
		}

		/**
		 * @param param Generic parameter.
		 */
		public void object(final Object param) {
			//
		}

		/**
		 * @param param Multipart parameter.
		 */
		public void multipart(@Multipart final Object param) {
			//
		}

		/**
		 * @param params Object array parameter.
		 */
		public void array(final Object[] params) {
			//
		}

		/**
		 * @param params Object collection parameter.
		 */
		public void collection(final Collection<Object> params) {
			//
		}

		/**
		 * @param param Path parameter.
		 */
		public void excluded(final @PathParam("p") Object param) {
			//
		}

		/**
		 * @param param Simple not null String parameter.
		 */
		public void jsr349Simple(final @NotNull String param) {
			//
		}

		/**
		 * @param param Simple optional String parameter.
		 */
		public void jsr349NullableObject(final @Nullable Object param) {
			//
		}

		/**
		 * @param param Simple not null path parameter parameter.
		 */
		public void jsr349Object(final @NotNull @PathParam("p") Object param) {
			//
		}

		/**
		 * @param params Collection parameter with several constraints.
		 */
		public void jsr349Collection(final @NotEmpty @Size(max = 2) Collection<Object> params) {
			//
		}

		/**
		 * @param context Security context.
		 */
		public void jsr349Context(final @Context SecurityContext context) {
			//
		}

	}

	private Method fromName(final String name) {
		return ClassUtils.getMethodIfAvailable(TestClass.class, name, (Class<?>[]) null);
	}

	private static final TestClass INSTANCE = new TestClass();
	private static final Message MESSAGE = Mockito.mock(Message.class);
	static {
		Mockito.when(MESSAGE.getExchange()).thenReturn(Mockito.mock(Exchange.class));
	}

	@Test
	void excluded() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("excluded"),
				Collections.singletonList("p"));
	}

	@Test
	void excludedNull() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("excluded"),
				Arrays.asList(new Object[] { null }));
	}

	@Test
	void jsr349Simple() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Simple"),
				Collections.singletonList("p"));
	}

	@Test
	void jsr349SimpleInvalid() {
		final var method = fromName("jsr349Simple");
		final var list = Arrays.asList(new Object[] { null });
		final var cve = Assertions.assertThrows(ConstraintViolationException.class,
				() -> validationInInterceptor.handleValidation(MESSAGE, INSTANCE, method, list));

		// Check all expected errors are there.
		final var constraintViolations = cve.getConstraintViolations();
		Assertions.assertNotNull(constraintViolations);
		Assertions.assertEquals(1, constraintViolations.size());

		// Check expected errors
		final var error1 = constraintViolations.iterator().next();
		Assertions.assertEquals(NotNull.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
		Assertions.assertEquals("jsr349Simple.param", error1.getPropertyPath().toString());
	}

	/**
	 * Check no parameter and not web service operation passes.
	 */
	@Test
	void empty() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("empty"), new ArrayList<>());
	}

	/**
	 * Check <code>null</code> parameter operation passes.
	 */
	@Test
	void queryObject() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("queryObject"),
				Arrays.asList(new Object[] { null }));
	}

	/**
	 * Check <code>null</code> parameter operation passes.
	 */
	@Test
	void multipart() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("multipart"),
				Arrays.asList(new Object[] { null }));
	}

	/**
	 * Check <code>null</code> parameter operation passes.
	 */
	@Test
	void simple() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("simple"), Collections.singletonList(""));
	}

	/**
	 * Check validation errors success.
	 */
	@Test
	void object() {
		final var userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("object"),
				Collections.singletonList(userDto));
	}

	/**
	 * Check validation will <code>null</code> context object -> still valid.
	 */
	@Test
	void jsr349Context() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Context"),
				Arrays.asList(new Object[] { null }));
	}

	/**
	 * Check not valid parameter operation failed.
	 */
	@Test
	void objectInvalid() {
		final var userDto = new SystemUser();
		userDto.setLogin("");
		final var method = fromName("object");
		final List<Object> beans = Collections.singletonList(userDto);
		final var cve = Assertions.assertThrows(ConstraintViolationException.class,
				() -> validationInInterceptor.handleValidation(MESSAGE, INSTANCE, method, beans));

		// Check all expected errors are there.
		final var constraintViolations = cve.getConstraintViolations();
		Assertions.assertNotNull(constraintViolations);
		Assertions.assertEquals(1, constraintViolations.size());

		// Check expected errors
		final var error1 = constraintViolations.iterator().next();
		Assertions.assertEquals(NotEmpty.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
		Assertions.assertEquals("", error1.getInvalidValue());
		Assertions.assertEquals("login", error1.getPropertyPath().toString());
	}

	/**
	 * Check not valid parameter operation failed.
	 */
	@Test
	void objectNull() {
		final SystemUser userDto = null;
		final var method = fromName("object");
		final List<Object> beans = Collections.singletonList(userDto);
		final var cve = Assertions.assertThrows(ConstraintViolationException.class,
				() -> validationInInterceptor.handleValidation(MESSAGE, INSTANCE, method, beans));

		// Check all expected errors are there.
		final var constraintViolations = cve.getConstraintViolations();
		Assertions.assertNotNull(constraintViolations);
		Assertions.assertEquals(1, constraintViolations.size());

		// Check expected errors
		final var error1 = constraintViolations.iterator().next();
		Assertions.assertEquals(NotNull.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
		Assertions.assertEquals("object.param", error1.getPropertyPath().toString());
	}

	/**
	 * Check validation errors success for arrays.
	 */
	@Test
	void array() {
		final var userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("object"),
				Arrays.asList(new Object[] { new Object[] { userDto } }));
	}

	/**
	 * Check validation errors success for arrays.
	 */
	@Test
	void collection() {
		final var userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("collection"),
				Collections.singletonList(Collections.singletonList(userDto)));
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	void jsr349Collection() {
		final var userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Collection"),
				Collections.singletonList(Collections.singletonList(userDto)));
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	void jsr349Object() {
		final var userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Object"),
				Collections.singletonList(userDto));
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	void jsr349ObjectInvalid() {
		final var userDto = new SystemUser();
		userDto.setLogin("junit");
		final var method = fromName("jsr349Object");
		final var objects = Arrays.asList(new Object[] { null });
		final var cve = Assertions.assertThrows(ConstraintViolationException.class,
				() -> validationInInterceptor.handleValidation(MESSAGE, INSTANCE, method, objects));

		// Check all expected errors are there.
		final var constraintViolations = cve.getConstraintViolations();
		Assertions.assertNotNull(constraintViolations);
		Assertions.assertEquals(1, constraintViolations.size());

		// Check expected errors
		final var error1 = constraintViolations.iterator().next();
		Assertions.assertEquals(NotNull.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
		Assertions.assertEquals("jsr349Object.p", error1.getPropertyPath().toString());
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	void jsr349CollectionInvalid() {
		final var userDto = new SystemUser();
		userDto.setLogin("junit");
		final var method = fromName("jsr349Collection");
		final List<Object> beans = Collections.singletonList(Arrays.asList(userDto, userDto, userDto));
		final var cve = Assertions.assertThrows(ConstraintViolationException.class,
				() -> validationInInterceptor.handleValidation(MESSAGE, INSTANCE, method, beans));

		// Check all expected errors are there.
		final var constraintViolations = cve.getConstraintViolations();
		Assertions.assertNotNull(constraintViolations);
		Assertions.assertEquals(1, constraintViolations.size());

		// Check expected errors
		final var error1 = constraintViolations.iterator().next();
		Assertions.assertEquals(Size.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
		Assertions.assertEquals("jsr349Collection.params", error1.getPropertyPath().toString());
	}

}
