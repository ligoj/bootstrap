/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link HasParameterRequestMatcher}
 */
class HasParameterRequestMatcherTest {

	@Test
    void testMatches() {
        var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("parameter")).thenReturn("value");
		Assertions.assertTrue(new HasParameterRequestMatcher("parameter").matches(request));
	}

	@Test
    void testBlank() {
        var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getParameter("parameter")).thenReturn(" ");
		Assertions.assertFalse(new HasParameterRequestMatcher("parameter").matches(request));
	}
}
