/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import java.util.regex.Pattern;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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
	 * Matcher for MySQL/MariaDB errors like :
	 * <code>`assignment`, CONSTRAINT `FK3D2B86CDAF555D0B` FOREIGN KEY (`project`)</code>
	 */
	private static final Pattern PATTERN_FOREIGN_KEY = Pattern.compile("`(\\w+)`, CONSTRAINT `.*` FOREIGN KEY \\(`(\\w+)`\\)");

	/**
	 * Matcher for MySQL/MariaDB errors like : <code>Duplicate entry 'AA-EE' for key 'UniqueName'</code>
	 */
	private static final Pattern PATTERN_UNICITY = Pattern.compile("Duplicate entry '([^']+)' for key '([^']+)'");

	/**
	 * Matcher for PostgreSQL unique constraint errors like :
	 * <code>Key (parameter, node)=(service:prov:aws:access-key-id, service:prov:aws:test) already exists.</code>
	 * Group 1 holds the constraint columns, group 2 the offending values.
	 */
	private static final Pattern PATTERN_PG_UNICITY = Pattern.compile("Key \\(([^)]+)\\)=\\((.+)\\) already exists");

	/**
	 * Matcher for PostgreSQL foreign key errors like :
	 * <code>Key (project)=(5) is still referenced from table "assignment".</code> (delete/update) or
	 * <code>Key (project)=(5) is not present in table "project".</code> (insert/update).
	 * Group 1 holds the offending column, group 2 the related table.
	 */
	private static final Pattern PATTERN_PG_FOREIGN_KEY = Pattern
			.compile("Key \\(([^)]+)\\)=\\(.+\\) is (?:still referenced from|not present in) table \"([^\"]+)\"");

	@Override
	public Response toResponse(final DataIntegrityViolationException exception) {
		log.error("DataIntegrityViolationException exception", exception);
		final var root = ExceptionUtils.getRootCause(exception);
		final var message = StringUtils.trimToEmpty(root == null ? exception.getMessage() : root.getMessage());

		// Foreign key violation : MySQL/MariaDB syntax, then PostgreSQL syntax
		var matcher = PATTERN_FOREIGN_KEY.matcher(message);
		if (matcher.find()) {
			return toResponse("foreign", exception, matcher.group(1) + "/" + matcher.group(2));
		}
		matcher = PATTERN_PG_FOREIGN_KEY.matcher(message);
		if (matcher.find()) {
			// PostgreSQL reports the referencing/referenced table and the column;
			// map them to the same from/to order as MySQL (table/column).
			return toResponse("foreign", exception, matcher.group(2) + "/" + matcher.group(1));
		}

		// Unicity violation : MySQL/MariaDB syntax, then PostgreSQL syntax
		matcher = PATTERN_UNICITY.matcher(message);
		if (matcher.find()) {
			return toResponse("unicity", exception, matcher.group(1) + "/" + matcher.group(2));
		}
		matcher = PATTERN_PG_UNICITY.matcher(message);
		if (matcher.find()) {
			// PostgreSQL reports "Key (columns)=(values) already exists"; map to the
			// same entry/name order as MySQL ("Duplicate entry '<values>' for key '<name>'").
			return toResponse("unicity", exception, matcher.group(2) + "/" + matcher.group(1));
		}

		// Another unmanaged SQL error : keep the raw message (and cause chain)
		return toResponse("unknown", exception, null);
	}

	/**
	 * Build the {@link Response} for a managed integrity error.
	 *
	 * @param code      the integrity sub-code ({@code foreign}, {@code unicity} or {@code unknown}).
	 * @param exception the source exception, its cause feeds the technical message/cause chain.
	 * @param message   the parsed <code>&lt;entry&gt;/&lt;name&gt;</code> message, or {@code null} to keep
	 *                  the raw technical message of the cause.
	 */
	private Response toResponse(final String code, final Throwable exception, final String message) {
		final var serverError = new ServerError();
		serverError.setCode("integrity-" + code);
		serverError.setThrowable(exception.getCause());
		if (message != null) {
			serverError.setMessage(message);
		}
		return toResponse(Status.PRECONDITION_FAILED, serverError);
	}

}
