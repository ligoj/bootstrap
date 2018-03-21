/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * Component able to generate CSV data from JPA entity - the managed properties - and also the standard Java Beans. This
 * operation is a two-ways transformation able to create Java Beans from CSV file having or not headers.
 */
public abstract class AbstractCsvManager {

	/**
	 * Default CSV encoding.
	 */
	public static final String DEFAULT_ENCODING = "cp1250";

	/**
	 * Return a list of JPA bean read from the given CSV file. Headers are expected.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param resource
	 *            the CSV input resource readable from current class loader.
	 * @return the read beans.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toBean(final Class<T> beanType, final String resource) throws IOException {
		Reader input = null;
		try {
			input = new InputStreamReader(new ClassPathResource(resource).getInputStream(), DEFAULT_ENCODING);
			return toBean(beanType, input);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Return a list of JPA bean re ad from the given CSV input. Headers are expected.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param input
	 *            the CSV input.
	 * @return A new bean instance of type T.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public abstract <T> List<T> toBean(Class<T> beanType, Reader input) throws IOException;

	/**
	 * Writes to the given writer the given items in CSV format with a header. The generator use Java Bean properties
	 * having a writable method. If managed JPA properties are expected, see {@link #toCsv(List, Class, Writer)}.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param items
	 *            Java Beans to write.
	 * @param beanType
	 *            Java Beans type. Is expected because of dynamic or multiple type of provided items.
	 * @param result
	 *            the target writer.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> void toCsv(final List<T> items, final Class<T> beanType, final Writer result) throws IOException {
		// Build descriptor list respecting the declaration order
		final List<String> descriptorsOrdered = Arrays.stream(BeanUtils.getPropertyDescriptors(beanType))
				.filter(property -> property.getWriteMethod() != null).map(PropertyDescriptor::getName).collect(Collectors.toList());
		String[] headers = new String[descriptorsOrdered.size()];
		headers = descriptorsOrdered.toArray(headers);

		// Write the data
		final CsvBeanWriter writer = new CsvBeanWriter(result);
		writer.writeHeader(headers);
		for (final T item : items) {
			writer.write(headers, item);
		}
		result.flush();
	}

}
