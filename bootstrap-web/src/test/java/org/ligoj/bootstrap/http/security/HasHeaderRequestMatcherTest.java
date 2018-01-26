package org.ligoj.bootstrap.http.security;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link HasHeaderRequestMatcher}
 */
public class HasHeaderRequestMatcherTest {

	@Test
	public void testMatches() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn("value");
		Assertions.assertTrue(new HasHeaderRequestMatcher("header").matches(request));
	}

	@Test
	public void testBlank() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn(" ");
		Assertions.assertFalse(new HasHeaderRequestMatcher("header").matches(request));
	}
}
