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
public class JaxRsAnnotationParanamerTest {

	public static class TestClass {

		/**
		 * 
		 * @param p1 A path parameter.
		 * @param h1 A header parameter.
		 * @param f1 A form parameter.
		 * @param c1 A cookie parameter.
		 * @param q1 A query parameter.
		 * @param n1 Any named parameter.
		 * @param n A JSR-303 contraint parameter.
		 */
		public void test(final @PathParam("p") String p1, final @HeaderParam("h") String h1, final @FormParam("f") String f1,
				final @CookieParam("c") String c1, @QueryParam("q") String q1, @Named("n") String n1, @NotNull String n) {
			//
		}

	}

	private <T extends Annotation> T fromClass(final Class<T> annotationClass) {
		return Arrays.stream(ClassUtils.getMethodIfAvailable(TestClass.class, "test", (Class<?>[]) null).getParameters())
				.filter(p -> p.isAnnotationPresent(annotationClass)).findFirst().get().getAnnotation(annotationClass);
	}

	@Test
	public void isNamedPathParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(PathParam.class)));
	}

	@Test
	public void isNamedHeaderParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(HeaderParam.class)));
	}

	@Test
	public void isNamedFormParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(FormParam.class)));
	}

	@Test
	public void isNamedCookieParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(CookieParam.class)));
	}

	@Test
	public void isNamedQueryParam() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(QueryParam.class)));
	}

	@Test
	public void isNamedNamed() {
		Assertions.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(Named.class)));
	}

	@Test
	public void isNamedAny() {
		Assertions.assertFalse(new JaxRsAnnotationParanamer().isNamed(fromClass(NotNull.class)));
	}

	@Test
	public void getNamedValuePathParam() {
		Assertions.assertEquals("p", new JaxRsAnnotationParanamer().getNamedValue(fromClass(PathParam.class)));
	}

	@Test
	public void getNamedValueHeaderParam() {
		Assertions.assertEquals("h", new JaxRsAnnotationParanamer().getNamedValue(fromClass(HeaderParam.class)));
	}

	@Test
	public void getNamedValueFormParam() {
		Assertions.assertEquals("f", new JaxRsAnnotationParanamer().getNamedValue(fromClass(FormParam.class)));
	}

	@Test
	public void getNamedValueCookieParam() {
		Assertions.assertEquals("c", new JaxRsAnnotationParanamer().getNamedValue(fromClass(CookieParam.class)));
	}

	@Test
	public void getNamedValueQueryParam() {
		Assertions.assertEquals("q", new JaxRsAnnotationParanamer().getNamedValue(fromClass(QueryParam.class)));
	}

	@Test
	public void getNamedValueNamed() {
		Assertions.assertEquals("n", new JaxRsAnnotationParanamer().getNamedValue(fromClass(Named.class)));
	}

	@Test
	public void getNamedValueAny() {
		Assertions.assertNull(new JaxRsAnnotationParanamer().getNamedValue(fromClass(NotNull.class)));
	}

}
