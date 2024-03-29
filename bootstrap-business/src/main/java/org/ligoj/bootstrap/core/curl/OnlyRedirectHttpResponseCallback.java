/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;

/**
 * Only accept 302.
 */
public class OnlyRedirectHttpResponseCallback extends DefaultHttpResponseCallback {

	@Override
	protected boolean acceptResponse(final ClassicHttpResponse response) {
		return super.acceptResponse(response)
				&& acceptLocation(response.getFirstHeader("location") == null ? null : response.getFirstHeader("location").getValue());
	}

	@Override
	protected boolean acceptStatus(final int status) {
		return status == HttpServletResponse.SC_MOVED_TEMPORARILY;
	}

	/**
	 * Indicate the location is accepted.
	 * @param location The location to check.
	 * @return <code>true</code> when location is accepted.
	 */
	protected boolean acceptLocation(final String location) {
		// Accept all not null locations by default
		return location != null;
	}

}