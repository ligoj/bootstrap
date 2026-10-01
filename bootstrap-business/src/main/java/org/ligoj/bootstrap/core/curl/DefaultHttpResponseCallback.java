/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.curl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The default callback implementation. Stop the execution when a response with an entity has a status above 204 (see
 * {@link #acceptStatus(int)}). A response without entity is always accepted whatever its status. Store the last
 * received entity string when requested.
 */
@Slf4j
public class DefaultHttpResponseCallback implements HttpResponseCallback {

	/**
	 * Maximal logged characters of a rejected response body.
	 */
	public static final int MAX_LOG_BODY = 2048;

	/**
	 * Return the given body truncated to {@link #MAX_LOG_BODY} characters, with its full size.
	 *
	 * @param body The response body. May be <code>null</code>.
	 * @return The body to log.
	 */
	static String toLogBody(final String body) {
		if (body == null || body.length() <= MAX_LOG_BODY) {
			return body;
		}
		return body.substring(0, MAX_LOG_BODY) + "... (" + body.length() + " characters)";
	}

	@Override
	public boolean onResponse(final CurlRequest request, final ClassicHttpResponse response) throws IOException {

		// Read the response
		final var entity = response.getEntity();
		final var url = CurlProcessor.toLogUrl(request.getUrl());
		log.info("{} {}", response.getCode(), url);
		if (entity != null) {

			try {
				// Check the status
				if (!acceptResponse(response)) {
					// The body may echo sensitive data: only in debug, and truncated
					log.error("{} {} rejected", response.getCode(), url);
					log.debug("Rejected response body: {}", toLogBody(EntityUtils.toString(entity)));
					return false;
				}

				// Save the response as needed
				if (request.isSaveResponse()) {
					request.setResponse(EntityUtils.toString(entity, StandardCharsets.UTF_8));
				}

			} catch (final Exception pe) {
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
	 * @param response The received response.
	 * @return <code>true</code> to proceed the next request. <code>false</code> otherwise.
	 */
	protected boolean acceptResponse(final ClassicHttpResponse response) {
		return acceptStatus(response.getCode());
	}

	/**
	 * Indicate the status is accepted.
	 *
	 * @param status The received status to accept.
	 * @return <code>true</code> to proceed the next request. <code>false</code> otherwise.
	 */
	protected boolean acceptStatus(final int status) {
		return status <= HttpServletResponse.SC_NO_CONTENT;
	}

}
