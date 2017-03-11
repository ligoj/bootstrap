package org.ligoj.bootstrap.http.security;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link HasHeaderRequestMatcher}
 */
public class HasHeaderRequestMatcherTest {

	@Test
	public void testMatches() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn("value");
		Assert.assertTrue(new HasHeaderRequestMatcher("header").matches(request));
	}

	@Test
	public void testBlank() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn(" ");
		Assert.assertFalse(new HasHeaderRequestMatcher("header").matches(request));
	}
}
