/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.validation;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

/**
 * Test class of {@link JaxRsAnnotationParanamer}
 */
class JaxRsAnnotationParanamerTest {

	static class TestClass {

		/**
		 *
		 * @param p1 A path parameter.
		 * @param h1 A header parameter.
		 * @param f1 A form parameter.
		 * @param c1 A cookie parameter.
		 * @param q1 A query parameter.
		 * @param n A JSR-303 constraint parameter.
		 * @param i1 Named (CDI) parameter.
		 */
		public void test(final @PathParam("p") String p1, final @HeaderParam("h") String h1, final @FormParam("f") String f1,
				final @CookieParam("c") String c1, @QueryParam("q") String q1, @NotNull String n, @Named("i") String i1) {
			//
		}

	}

	private <T extends Annotation> T fromClass(final Class<T> annotationClass) {
		return Arrays.stream(ClassUtils.getMethodIfAvailable(TestClass.class, "test", (Class<?>[]) null).getParameters())
				.filter(p -> p.isAnnotationPresent(annotationClass)).findFirst().get().getAnnotation(annotationClass);
	}

	@Test
	void isNamedPathParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(PathParam.class)));
	}

	@Test
	void isNamedHeaderParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(HeaderParam.class)));
	}

	@Test
	void isNamedFormParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(FormParam.class)));
	}

	@Test
	void isNamedCookieParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(CookieParam.class)));
	}

	@Test
	void isNamedQueryParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(QueryParam.class)));
	}

	@Test
	void isNamedAny() {
		Assertions.assertFalse(new JaxRsAnnotationParanamer().isNamed(fromClass(NotNull.class)));
	}

	@Test
	void isNamedInject() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(Named.class)));
	}

	@Test
	void getNamedValuePathParam() {
		Assertions.assertEquals("p", new JaxRsAnnotationParanamer().getNamedValue(fromClass(PathParam.class)));
	}

	@Test
	void getNamedValueHeaderParam() {
		Assertions.assertEquals("h", new JaxRsAnnotationParanamer().getNamedValue(fromClass(HeaderParam.class)));
	}

	@Test
	void getNamedValueFormParam() {
		Assertions.assertEquals("f", new JaxRsAnnotationParanamer().getNamedValue(fromClass(FormParam.class)));
	}

	@Test
	void getNamedValueInject() {
		Assertions.assertEquals("i", new JaxRsAnnotationParanamer().getNamedValue(fromClass(Named.class)));
	}

	@Test
	void getNamedValueCookieParam() {
		Assertions.assertEquals("c", new JaxRsAnnotationParanamer().getNamedValue(fromClass(CookieParam.class)));
	}

	@Test
	void getNamedValueQueryParam() {
		Assertions.assertEquals("q", new JaxRsAnnotationParanamer().getNamedValue(fromClass(QueryParam.class)));
	}

	@Test
	void getNamedValueAny() {
		Assertions.assertNull(new JaxRsAnnotationParanamer().getNamedValue(fromClass(NotNull.class)));
	}

}
