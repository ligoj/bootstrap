package org.ligoj.bootstrap.http.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * Matches when the current request contains a specific parameter.
 */
public class HasParameterRequestMatcher implements org.springframework.security.web.util.matcher.RequestMatcher {

	private String parameter;

	/**
	 * Takes a required parameter name. Case is sensitive.
	 * 
	 * @param parameter
	 *            the parameter name.
	 */
	public HasParameterRequestMatcher(final String parameter) {
		this.parameter = parameter;
	}

	@Override
	public boolean matches(final HttpServletRequest request) {
		return StringUtils.isNotBlank(request.getParameter(parameter));
	}

}
