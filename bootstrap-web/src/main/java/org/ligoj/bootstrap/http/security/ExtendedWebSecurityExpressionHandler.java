/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

/**
 * Custom expression handler using a different expression manager.
 */
@Component
public class ExtendedWebSecurityExpressionHandler extends AbstractCommonSecurityExpressionHandler<RequestAuthorizationContext> {
	@Override
	protected SecurityExpressionOperations createSecurityExpressionRoot(final Authentication authentication, final RequestAuthorizationContext fi) {
		return complete(new ExtendedWebSecurityExpressionRoot(() -> authentication, fi.getRequest()));
	}

}
