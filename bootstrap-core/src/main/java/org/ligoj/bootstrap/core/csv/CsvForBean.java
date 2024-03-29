/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.stereotype.Component;

/**
 * Component able to generate CSV data from JPA entity - the managed properties - and also the standard Java Beans. This
 * operation is a two-ways transformation able to create Java Beans from CSV file having or not headers.
 */
@Component
public class CsvForBean extends AbstractCsvManager {

	@Override
	public <T> List<T> toBean(final Class<T> beanType, final Reader input, final TriConsumer<T, String, String> setter)
			throws IOException {
		final List<T> result = new ArrayList<>();
		final var inputProxy = new BufferedReader(input);
		final var line = inputProxy.readLine();
		if (line == null) {
			// No content means no header, no items.
			return result;
		}

		final var reader = new CsvBeanReader<>(inputProxy, beanType,
				StringUtils.splitPreserveAllTokens(line, CsvReader.DEFAULT_SEPARATOR));

		// Build all instances
		fillList(result, reader, setter);
		return result;
	}

	/**
	 * Read the next bean from the given reader.
	 *
	 * @param <T>
	 *            Target bean type.
	 * @param reader
	 *            The CSV reader.
	 * @return The instance. May be <code>null</code> with EOF.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> T toBean(final CsvBeanReader<T> reader) throws IOException {
		return toBean(reader, null);
	}

	/**
	 * Read the next bean from the given reader.
	 *
	 * @param <T>
	 *            Target bean type.
	 * @param reader
	 *            The CSV reader.
	 * @param setter
	 *            Optional setter for raw properties.
	 * @return The instance. May be <code>null</code> with EOF.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> T toBean(final CsvBeanReader<T> reader, final TriConsumer<T, String, String> setter) throws IOException {
		return reader.read(setter);
	}

	private <T> void fillList(final List<T> result, final CsvBeanReader<T> reader,
			final TriConsumer<T, String, String> setter) throws IOException {
		// Build the first instance
        var order = toBean(reader, setter);
		while (order != null) {
			result.add(order);

			// Read the next one
			order = toBean(reader, setter);
		}
	}

}
