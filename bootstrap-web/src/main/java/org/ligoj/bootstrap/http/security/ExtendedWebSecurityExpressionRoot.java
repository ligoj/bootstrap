/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

/**
 * Extended expression to but used in method and authentication voter.
 */
public class ExtendedWebSecurityExpressionRoot extends WebSecurityExpressionRoot {

	/**
	 * Creates an instance for the given {@link Supplier} of the {@link Authentication}
	 * and {@link HttpServletRequest}.
	 * @param authentication the {@link Supplier} of the {@link Authentication} to use
	 * @param context the {@link RequestAuthorizationContext} to use
	 * @since 7.0
	 */
	public ExtendedWebSecurityExpressionRoot(Supplier<? extends @Nullable Authentication> authentication,
			RequestAuthorizationContext context) {
		super(authentication, context);
	}

	/**
	 * Validate the current request a defined parameter.
	 *
	 * @param parameter
	 *            the expected non-blank parameter.
	 * @return true if the parameter of the current request is not blank.
	 */
	public boolean hasParameter(final String parameter) {
		return new HasParameterRequestMatcher(parameter).matches(request);
	}

	/**
	 * Validate the current request a defined header.
	 *
	 * @param header
	 *            the expected non-blank header.
	 * @return true if the header of the current request is not blank.
	 */
	public boolean hasHeader(final String header) {
		return new HasHeaderRequestMatcher(header).matches(request);
	}

}
