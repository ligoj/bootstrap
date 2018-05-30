/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.io.Reader;

import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * CSV reader implementation based on Camel implementation (see BindyCsvDataFormat) where some issues have been fixed.
 *
 * @param <T>
 *            Bean type.
 */
public class CsvBeanReader<T> extends AbstractCsvReader<T> {

	/**
	 * Simple override.
	 *
	 * @param reader
	 *            Input reader.
	 * @param beanType
	 *            Class of bean to build.
	 * @param headers
	 *            Headers, an ordered property list. Header with <code>null</code> or empty name will skip the
	 *            corresponding column.
	 */
	public CsvBeanReader(final Reader reader, final Class<T> beanType, final String... headers) {
		super(reader, beanType, headers);
	}

	@Override
	protected void setProperty(final T bean, final String property, final String rawValue) throws ReflectiveOperationException {
		final int fkeyIndex = property.indexOf('.');
		if (fkeyIndex == -1) {
			setSimpleProperty(bean, property, rawValue);
		} else {
			throw new TechnicalException("Foreign key management is not supported in bean mode, use CsvJpaReader");
		}
	}
}