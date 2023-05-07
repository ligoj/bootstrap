/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The default callback implementation. Stop the execution when a status above 302 is received. Store the last received
 * entity string.
 */
@Slf4j
public class DefaultHttpResponseCallback implements HttpResponseCallback {

	@Override
	public boolean onResponse(final CurlRequest request, final CloseableHttpResponse response) throws IOException {

		// Read the response
		final var entity = response.getEntity();
		log.info("{} {}", response.getCode(), request.getUrl());
		if (entity != null) {

			try {
				// Check the status
				if (!acceptResponse(response)) {
					log.error(EntityUtils.toString(entity));
					return false;
				}

				// Save the response as needed
				if (request.isSaveResponse()) {
					request.setResponse(EntityUtils.toString(entity, StandardCharsets.UTF_8));
				}

			} catch(final ParseException pe) {
				log.error("Unable to parse the response", pe);
				return false;
			} finally {
				entity.getContent().close();
			}
		}
		return true;
	}

	/**
	 * Indicate the response is accepted.
	 * 
	 * @param response
	 *            The received response.
	 * @return <code>true</code> to proceed the next request. <code>false</code> otherwise.
	 */
	protected boolean acceptResponse(final CloseableHttpResponse response) {
		return acceptStatus(response.getCode());
	}

	/**
	 * Indicate the status is accepted.
	 * 
	 * @param status
	 *            The received status to accept.
	 * @return <code>true</code> to proceed the next request. <code>false</code> otherwise.
	 */
	protected boolean acceptStatus(final int status) {
		return status <= HttpServletResponse.SC_NO_CONTENT;
	}

}
