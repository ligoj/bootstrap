/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import lombok.AllArgsConstructor;
import org.apache.hc.core5.http.ClassicHttpResponse;

import java.io.IOException;

/**
 * This callback get the header from the response.
 */
@AllArgsConstructor
public class HeaderHttpResponseCallback extends DefaultHttpResponseCallback {

	/**
	 * The header name to save as response.
	 */
	private final String header;

	@Override
	public boolean onResponse(final CurlRequest request, final ClassicHttpResponse response) throws IOException {
		super.onResponse(request, response);
		// Response is pre-checked
		final var value = response.getFirstHeader(header);
		if (value == null) {
			// Header is not present
			request.setResponse(null);
			return false;
		}
		// Extract the value and save it in the response
		request.setResponse(value.getValue());
		return true;
	}

}
