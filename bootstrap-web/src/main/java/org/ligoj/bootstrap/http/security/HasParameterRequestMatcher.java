/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.http.security;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;

/**
 * Matches when the current request contains a specific parameter.
 */
@AllArgsConstructor
public class HasParameterRequestMatcher implements org.springframework.security.web.util.matcher.RequestMatcher {

	private final String parameter;

	@Override
	public boolean matches(final HttpServletRequest request) {
		return StringUtils.isNotBlank(request.getParameter(parameter));
	}

}
