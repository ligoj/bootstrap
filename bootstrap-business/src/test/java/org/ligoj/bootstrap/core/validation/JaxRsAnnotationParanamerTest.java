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

import org.junit.Test;
import org.springframework.util.ClassUtils;

/**
 * Test class of {@link JaxRsAnnotationParanamer}
 */
public class JaxRsAnnotationParanamerTest {

	@SuppressWarnings("unused")
	public static class TestClass {

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
		org.junit.Assert.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(PathParam.class)));
	}

	@Test
	public void isNamedHeaderParam() {
		org.junit.Assert.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(HeaderParam.class)));
	}

	@Test
	public void isNamedFormParam() {
		org.junit.Assert.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(FormParam.class)));
	}

	@Test
	public void isNamedCookieParam() {
		org.junit.Assert.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(CookieParam.class)));
	}

	@Test
	public void isNamedQueryParam() {
		org.junit.Assert.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(QueryParam.class)));
	}

	@Test
	public void isNamedNamed() {
		org.junit.Assert.assertTrue(new JaxRsAnnotationParanamer().isNamed(fromClass(Named.class)));
	}

	@Test
	public void isNamedAny() {
		org.junit.Assert.assertFalse(new JaxRsAnnotationParanamer().isNamed(fromClass(NotNull.class)));
	}

	@Test
	public void getNamedValuePathParam() {
		org.junit.Assert.assertEquals("p", new JaxRsAnnotationParanamer().getNamedValue(fromClass(PathParam.class)));
	}

	@Test
	public void getNamedValueHeaderParam() {
		org.junit.Assert.assertEquals("h", new JaxRsAnnotationParanamer().getNamedValue(fromClass(HeaderParam.class)));
	}

	@Test
	public void getNamedValueFormParam() {
		org.junit.Assert.assertEquals("f", new JaxRsAnnotationParanamer().getNamedValue(fromClass(FormParam.class)));
	}

	@Test
	public void getNamedValueCookieParam() {
		org.junit.Assert.assertEquals("c", new JaxRsAnnotationParanamer().getNamedValue(fromClass(CookieParam.class)));
	}

	@Test
	public void getNamedValueQueryParam() {
		org.junit.Assert.assertEquals("q", new JaxRsAnnotationParanamer().getNamedValue(fromClass(QueryParam.class)));
	}

	@Test
	public void getNamedValueNamed() {
		org.junit.Assert.assertEquals("n", new JaxRsAnnotationParanamer().getNamedValue(fromClass(Named.class)));
	}

	@Test
	public void getNamedValueAny() {
		org.junit.Assert.assertNull(new JaxRsAnnotationParanamer().getNamedValue(fromClass(NotNull.class)));
	}

}
