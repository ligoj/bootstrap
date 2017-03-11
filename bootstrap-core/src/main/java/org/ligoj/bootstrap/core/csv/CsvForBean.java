package org.ligoj.bootstrap.core.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Component able to generate CSV data from JPA entity - the managed properties - and also the standard Java Beans. This
 * operation is a two-ways transformation able to create Java Beans from CSV file having or not headers.
 */
@Component
public class CsvForBean extends AbstractCsvManager {

	/**
	 * Return a list of JPA bean re ad from the given CSV input. Headers are expected. {@inheritDoc}
	 */
	@Override
	public <T> List<T> toBean(final Class<T> beanType, final Reader input) throws IOException {
		final List<T> result = new ArrayList<>();
		final Reader inputProxy = new BufferedReader(input);
		final String line = ((BufferedReader) inputProxy).readLine();
		if (line == null) {
			// No content means no header, no items.
			return result;
		}

		final CsvBeanReader<T> reader = new CsvBeanReader<>(inputProxy, beanType, StringUtils.splitPreserveAllTokens(line, CsvBeanWriter.SEPARATOR));

		// Build all instances
		fillList(result, reader);
		return result;
	}

	private <T> void fillList(final List<T> result, final CsvBeanReader<T> reader) throws IOException {
		// Build the first instance
		T order = reader.read();
		while (order != null) {
			result.add(order);

			// Read the next one
			order = reader.read();
		}
	}

}
