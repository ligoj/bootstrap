/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.core.resource.ServerError;
import org.springframework.dao.DataIntegrityViolationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles database integrity issue to a JSON string.
 */
@Provider
@Slf4j
public class DataIntegrityViolationExceptionMapper extends AbstractMapper implements ExceptionMapper<DataIntegrityViolationException> {

	/**
	 * Matcher for errors like : <code>`assignment`, CONSTRAINT `FK3D2B86CDAF555D0B` FOREIGN KEY (`project`)</code>
	 */
	private static final Pattern PATTERN_FOREIGN_KEY = Pattern.compile("`(\\w+)`, CONSTRAINT `.*` FOREIGN KEY \\(`(\\w+)`\\)");

	/**
	 * Matcher for errors like : <code>Duplicate entry 'AA-EE' for key 'UniqueName'</code>
	 */
	private static final Pattern PATTERN_UNICITY = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)'");

	@Override
	public Response toResponse(final DataIntegrityViolationException exception) {
		log.error("DataIntegrityViolationException exception", exception);
		final Throwable root = ExceptionUtils.getRootCause(exception);
		Matcher matcher = PATTERN_FOREIGN_KEY.matcher(StringUtils.trimToEmpty(root.getMessage()));
		final String code;
		if (matcher.find()) {
			// Foreign key
			code = "foreign";
		} else {
			matcher = PATTERN_UNICITY.matcher(root.getMessage());
			if (matcher.find()) {
				// Duplicate entry
				code = "unicity";
			} else {
				// Another SQL error
				code = "unknown";
				matcher = null;
			}
		}

		return toResponse(Status.PRECONDITION_FAILED, newServerError(exception, matcher, code));
	}

	/**
	 * Build the server error instance.
	 */
	private ServerError newServerError(final Throwable exception, final Matcher matcher, final String code) {
		final ServerError serverError = new ServerError();
		serverError.setCode("integrity-" + code);
		serverError.setThrowable(exception.getCause());
		if (matcher != null) {
			serverError.setMessage(matcher.group(1) + "/" + matcher.group(2));
		}
		return serverError;
	}

}
