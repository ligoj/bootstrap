/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;

import java.io.IOException;

/**
 * {@link CloseableHttpResponse} callback used for each response whatever the status.
 */
@FunctionalInterface
public interface HttpResponseCallback {

	/**
	 * Called when a response is received.
	 * 
	 * @param request
	 *            the original request.
	 * @param response
	 *            the received response.
	 * @return <code>true</code> to proceed the next request. <code>false</code> otherwise.
	 * @throws IOException When response cannot be read.
	 */
	boolean onResponse(CurlRequest request, ClassicHttpResponse response) throws IOException;
}
