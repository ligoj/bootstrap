/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.io.Reader;

/**
 * CSV reader implementation based on Camel implementation where some issues have been fixed.
 *
 * @param <T> Bean type.
 */
public class CsvBeanReader<T> extends AbstractCsvReader<T> {

	/**
	 * Simple override.
	 *
	 * @param reader   Input reader.
	 * @param beanType Class of bean to build.
	 * @param headers  Headers, an ordered property list. Header with <code>null</code> or empty name will skip the
	 *                 corresponding column.
	 */
	public CsvBeanReader(final Reader reader, final Class<T> beanType, final String... headers) {
		super(reader, beanType, headers);
	}
}