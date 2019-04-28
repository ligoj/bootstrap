/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.template;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;
import org.ligoj.bootstrap.core.DateUtils;

/**
 * A {@link Processor} parsing a {@link String} to a {@link Date}.
 */
public class ParseDateProcessor extends Processor<String> {

	private final FastDateFormat format;

	/**
	 * Pattern and data constructor.
	 * 
	 * @param pattern
	 *            the pattern used to parse the input..
	 * @param data
	 *            the context data or another {@link Processor} instance.
	 */
	public ParseDateProcessor(final String pattern, final Object data) {
		super(data);
		this.format = FastDateFormat.getInstance(pattern, DateUtils.getApplicationTimeZone());
	}

	/**
	 * Pattern constructor.
	 * 
	 * @param pattern
	 *            the pattern used to parse the input..
	 */
	public ParseDateProcessor(final String pattern) {
		this(pattern, null);
	}

	@Override
	public Date getValue(final String context) {
		final var data = (String) super.getValue(context);
		try {
			return format.parse(data);
		} catch (final ParseException e) {
			// Invalid format of String
			throw new IllegalArgumentException("Invalid string '" + data + "' for format '" + format.getPattern() + "'", e);
		}
	}

}
