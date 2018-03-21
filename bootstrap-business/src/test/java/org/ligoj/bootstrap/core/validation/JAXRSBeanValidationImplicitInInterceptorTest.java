/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

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
public class JAXRSBeanValidationImplicitInInterceptorTest extends AbstractBootTest {

	@Autowired
	private JAXRSBeanValidationImplicitInInterceptor validationInInterceptor;

	public static class TestClass {
		public void empty() {
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
	{
		Mockito.when(MESSAGE.getExchange()).thenReturn(Mockito.mock(Exchange.class));
	}

	@Test
	public void excluded() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("excluded"), Arrays.asList("p"));
	}

	@Test
	public void excludedNull() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("excluded"), Arrays.asList(new Object[] { null }));
	}

	@Test
	public void jsr349Simple() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Simple"), Arrays.asList("p"));
	}

	@Test
	public void jsr349SimpleInvalid() {
		try {
			validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Simple"), Arrays.asList(new Object[] { null }));
			Assertions.fail("Expected validation errors");
		} catch (final ConstraintViolationException cve) {

			// Check all expected errors are there.
			final Set<ConstraintViolation<?>> constraintViolations = cve.getConstraintViolations();
			Assertions.assertNotNull(constraintViolations);
			Assertions.assertEquals(1, constraintViolations.size());

			// Check expected errors
			final ConstraintViolation<?> error1 = constraintViolations.iterator().next();
			Assertions.assertEquals(NotNull.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
			Assertions.assertEquals("jsr349Simple.param", error1.getPropertyPath().toString());
		}
	}

	/**
	 * Check no parameter and not web service operation passes.
	 */
	@Test
	public void empty() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("empty"), new ArrayList<>());
	}

	/**
	 * Check <tt>null</tt> parameter operation passes.
	 */
	@Test
	public void queryObject() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("queryObject"), Arrays.asList(new Object[] { null }));
	}

	/**
	 * Check <tt>null</tt> parameter operation passes.
	 */
	@Test
	public void multipart() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("multipart"), Arrays.asList(new Object[] { null }));
	}

	/**
	 * Check <tt>null</tt> parameter operation passes.
	 */
	@Test
	public void simple() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("simple"), Arrays.asList(""));
	}

	/**
	 * Check validation errors success.
	 */
	@Test
	public void object() {
		final SystemUser userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("object"), Arrays.asList(userDto));
	}

	/**
	 * Check validation will <code>null</code> context object -> still valid.
	 */
	@Test
	public void jsr349Context() {
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Context"), Arrays.asList(new Object[] { null }));
	}

	/**
	 * Check not valid parameter operation failed.
	 */
	@Test
	public void objectInvalid() {
		final SystemUser userDto = new SystemUser();
		userDto.setLogin("");
		try {
			validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("object"), Arrays.asList(userDto));
			Assertions.fail("Expected validation errors");
		} catch (final ConstraintViolationException cve) {

			// Check all expected errors are there.
			final Set<ConstraintViolation<?>> constraintViolations = cve.getConstraintViolations();
			Assertions.assertNotNull(constraintViolations);
			Assertions.assertEquals(1, constraintViolations.size());

			// Check expected errors
			final ConstraintViolation<?> error1 = constraintViolations.iterator().next();
			Assertions.assertEquals(NotEmpty.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
			Assertions.assertEquals("", error1.getInvalidValue());
			Assertions.assertEquals("login", error1.getPropertyPath().toString());
		}
	}

	/**
	 * Check not valid parameter operation failed.
	 */
	@Test
	public void objectNull() {
		final SystemUser userDto = null;
		try {
			validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("object"), Arrays.asList(userDto));
			Assertions.fail("Expected validation errors");
		} catch (final ConstraintViolationException cve) {

			// Check all expected errors are there.
			final Set<ConstraintViolation<?>> constraintViolations = cve.getConstraintViolations();
			Assertions.assertNotNull(constraintViolations);
			Assertions.assertEquals(1, constraintViolations.size());

			// Check expected errors
			final ConstraintViolation<?> error1 = constraintViolations.iterator().next();
			Assertions.assertEquals(NotNull.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
			Assertions.assertEquals("object.param", error1.getPropertyPath().toString());
		}
	}

	/**
	 * Check validation errors success for arrays.
	 */
	@Test
	public void array() {
		final SystemUser userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("object"), Arrays.asList(new Object[] { new Object[] { userDto } }));
	}

	/**
	 * Check validation errors success for arrays.
	 */
	@Test
	public void collection() {
		final SystemUser userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("collection"), Arrays.asList(Arrays.asList(userDto)));
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	public void jsr349Collection() {
		final SystemUser userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Collection"), Arrays.asList(Arrays.asList(userDto)));
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	public void jsr349Object() {
		final SystemUser userDto = new SystemUser();
		userDto.setLogin("junit");
		validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Object"), Arrays.asList(userDto));
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	public void jsr349ObjectInvalid() {
		try {
			final SystemUser userDto = new SystemUser();
			userDto.setLogin("junit");
			validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Object"), Arrays.asList(new Object[] { null }));
			Assertions.fail("Expected validation errors");
		} catch (final ConstraintViolationException cve) {

			// Check all expected errors are there.
			final Set<ConstraintViolation<?>> constraintViolations = cve.getConstraintViolations();
			Assertions.assertNotNull(constraintViolations);
			Assertions.assertEquals(1, constraintViolations.size());

			// Check expected errors
			final ConstraintViolation<?> error1 = constraintViolations.iterator().next();
			Assertions.assertEquals(NotNull.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
			Assertions.assertEquals("jsr349Object.p", error1.getPropertyPath().toString());
		}
	}

	/**
	 * Check validation errors success for collections.
	 */
	@Test
	public void jsr349CollectionInvalid() {
		try {
			final SystemUser userDto = new SystemUser();
			userDto.setLogin("junit");
			validationInInterceptor.handleValidation(MESSAGE, INSTANCE, fromName("jsr349Collection"),
					Arrays.asList(Arrays.asList(userDto, userDto, userDto)));
			Assertions.fail("Expected validation errors");
		} catch (final ConstraintViolationException cve) {

			// Check all expected errors are there.
			final Set<ConstraintViolation<?>> constraintViolations = cve.getConstraintViolations();
			Assertions.assertNotNull(constraintViolations);
			Assertions.assertEquals(1, constraintViolations.size());

			// Check expected errors
			final ConstraintViolation<?> error1 = constraintViolations.iterator().next();
			Assertions.assertEquals(Size.class, error1.getConstraintDescriptor().getAnnotation().annotationType());
			Assertions.assertEquals("jsr349Collection.params", error1.getPropertyPath().toString());
		}
	}

}
