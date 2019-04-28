/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.stereotype.Component;

/**
 * Custom expression handler using a different expression manager.
 */
@Component
public class ExtendedSecurityExpressionHandler extends AbstractSecurityExpressionHandler<FilterInvocation> {

	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

	@Override
	protected SecurityExpressionOperations createSecurityExpressionRoot(final Authentication authentication, final FilterInvocation fi) {
		
		// There we use the extended version of expression manager
		final WebSecurityExpressionRoot root = new ExtendedWebSecurityExpressionRoot(authentication, fi);
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		root.setDefaultRolePrefix("ROLE_");
		return root;
	}

}
