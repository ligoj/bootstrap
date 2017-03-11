package org.ligoj.bootstrap.core.template;

import java.text.Format;

/**
 * A {@link Processor} formatting an object to a string .
 * 
 * @param <T>
 *            the context bean type.
 */
public class FormatProcessor<T> extends Processor<T> {

	private final Format format;

	/**
	 * Format and data constructor.
	 * 
	 * @param format
	 *            the formatter.
	 * @param data
	 *            the context data or another {@link Processor} instance.
	 */
	public FormatProcessor(final Format format, final Object data) {
		super(data);
		this.format = format;
	}

	/**
	 * Format constructor.
	 * 
	 * @param format
	 *            the formatter.
	 */
	public FormatProcessor(final Format format) {
		this(format, null);
	}

	@Override
	public String getValue(final T context) {
		return format.format(super.getValue(context));
	}

}
