/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;

/**
 * Extended expression to but used in method and authentication voter.
 */
public class ExtendedWebSecurityExpressionRoot extends WebSecurityExpressionRoot {

	/**
	 * Takes authentication and request contexts.
	 * 
	 * @param authentication
	 *            the current authentication. Might be anonymous.
	 * @param invocation
	 *            current invocation. Can be method or request.
	 */
	public ExtendedWebSecurityExpressionRoot(final Authentication authentication, final FilterInvocation invocation) {
		super(authentication, invocation);
	}

	/**
	 * Validate the current request a defined parameter.
	 *
	 * @param parameter
	 *            the expected non blank parameter.
	 * @return true if the parameter of the current request is not blank.
	 */
	public boolean hasParameter(final String parameter) {
		return new HasParameterRequestMatcher(parameter).matches(request);
	}

	/**
	 * Validate the current request a defined header.
	 *
	 * @param header
	 *            the expected non blank header.
	 * @return true if the header of the current request is not blank.
	 */
	public boolean hasHeader(final String header) {
		return new HasHeaderRequestMatcher(header).matches(request);
	}

}
