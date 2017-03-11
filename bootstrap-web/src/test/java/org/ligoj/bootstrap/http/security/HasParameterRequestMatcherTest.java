package org.ligoj.bootstrap.http.security;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link HasParameterRequestMatcher}
 */
public class HasParameterRequestMatcherTest {

	@Test
	public void testMatches() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("parameter")).thenReturn("value");
		Assert.assertTrue(new HasParameterRequestMatcher("parameter").matches(request));
	}

	@Test
	public void testBlank() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("parameter")).thenReturn(" ");
		Assert.assertFalse(new HasParameterRequestMatcher("parameter").matches(request));
	}
}
