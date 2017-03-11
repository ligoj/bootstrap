package org.ligoj.bootstrap.http.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * Matches when the current request contains a specific header.
 */
public class HasHeaderRequestMatcher implements org.springframework.security.web.util.matcher.RequestMatcher {

	private String header;

	/**
	 * Takes a required header name. Case is not sensitive.
	 * 
	 * @param header
	 *            the header name.
	 */
	public HasHeaderRequestMatcher(final String header) {
		this.header = header;
	}

	@Override
	public boolean matches(final HttpServletRequest request) {
		return StringUtils.isNotBlank(request.getHeader(header));
	}

}
