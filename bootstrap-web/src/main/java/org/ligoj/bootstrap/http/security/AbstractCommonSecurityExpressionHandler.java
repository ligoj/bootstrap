/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;

/**
 * Custom expression handler using a different expression manager.
 */
public abstract class AbstractCommonSecurityExpressionHandler<T> extends AbstractSecurityExpressionHandler<T> {

	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

	/**
	 * Complete the root object with resolver, and other attributes.
	 *
	 * @param root The object to complete.
	 * @return the given root parameter for chaining.
	 */
	protected WebSecurityExpressionRoot complete(final WebSecurityExpressionRoot root) {
		root.setPermissionEvaluator(getPermissionEvaluator());
		root.setTrustResolver(trustResolver);
		root.setRoleHierarchy(getRoleHierarchy());
		root.setDefaultRolePrefix("ROLE_");
		return root;
	}

}
