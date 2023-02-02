/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link HasHeaderRequestMatcher}
 */
class HasHeaderRequestMatcherTest {

	@Test
    void testMatches() {
        var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn("value");
		Assertions.assertTrue(new HasHeaderRequestMatcher("header").matches(request));
	}

	@Test
    void testBlank() {
        var request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getHeader("header")).thenReturn(" ");
		Assertions.assertFalse(new HasHeaderRequestMatcher("header").matches(request));
	}
}
