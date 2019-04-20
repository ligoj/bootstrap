/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.text.StringEscapeUtils;
import org.ligoj.bootstrap.core.resource.TechnicalException;

import jodd.bean.BeanUtil;

/**
 * Basic writer extension supporting <code>null</code> values.
 */
public class CsvBeanWriter {

	private final Writer writer;

	/**
	 * Simple writer wrapper constructor.
	 *
	 * @param writer
	 *            the target writer.
	 */
	public CsvBeanWriter(final Writer writer) {
		this.writer = writer;
	}

	/**
	 * Write all
	 *
	 * @param headers
	 *            headers corresponding to java bean properties.
	 * @param content
	 *            object to write.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public void write(final String[] headers, final Object... content) throws IOException {
		for (final Object o : content) {
			writeLine(headers, o);
			writer.write('\n');
			write(null);
		}
	}

	/**
	 * Write a line corresponding to the given object.
	 */
	private void writeLine(final String[] headers, final Object o) {
		boolean first = true;
		try {
			for (final String header : headers) {
				writeSeparator(first);
				first = false;
				writeField(o, header);
			}
		} catch (final Exception e) {
			throw new TechnicalException("Unable to describe given object : " + o, e);
		}
	}

	/**
	 * Write a field
	 */
	private void writeField(final Object o, final String header)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		final Object value = BeanUtil.declaredSilent.getProperty(o, header);
		if (value != null) {
			writer.write(StringEscapeUtils.escapeCsv(value.toString()));
		}
	}

	/**
	 * Write a separator as necessary.
	 */
	private void writeSeparator(final boolean first) throws IOException {
		if (!first) {
			writer.write(CsvReader.DEFAULT_SEPARATOR);
		}
	}

	/**
	 * Write some values to the current writer.
	 *
	 * @param values
	 *            Strong values to write.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public void writeHeader(final String... values) throws IOException {
		boolean first = true;
		for (final String header : values) {
			writeSeparator(first);
			first = false;
			writer.write(header);
		}
		writer.write('\n');
	}

}
