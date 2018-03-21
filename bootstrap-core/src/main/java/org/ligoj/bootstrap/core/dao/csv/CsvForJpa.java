/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.csv.AbstractCsvManager;
import org.ligoj.bootstrap.core.csv.CsvBeanWriter;
import org.ligoj.bootstrap.core.csv.CsvReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/**
 * Component able to generate CSV data from JPA entity - the managed properties - and also the standard Java Beans. This
 * operation is a two-ways transformation able to create Java Beans from CSV file having or not headers.
 */
@Component
public class CsvForJpa extends AbstractCsvManager {

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = "pu")
	private EntityManager em;

	@Autowired
	protected JpaTransactionManager transactionManager;

	@Override
	public <T> List<T> toBean(final Class<T> beanType, final Reader input) throws IOException {
		return toJpa(beanType, input, true, false);
	}

	/**
	 * Return a list of JPA bean re ad from the given CSV input.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean type.
	 * @param input
	 *            the CSV input.
	 * @param hasHeader
	 *            when <tt>true</tt> the first row contains bean property names.
	 * @return the JPA beans built from CSV input.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toJpa(final Class<T> beanType, final Reader input, final boolean hasHeader) throws IOException {
		return toJpa(beanType, input, hasHeader, false);
	}

	/**
	 * Return a list of JPA bean read from the given CSV file. Headers are expected.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param resource
	 *            the CSV input resource readable from current class loader.
	 * @param hasHeader
	 *            when <tt>true</tt> the first row contains bean property names.
	 * @return the JPA beans built from CSV input.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toJpa(final Class<T> beanType, final String resource, final boolean hasHeader) throws IOException {
		return toJpa(beanType, resource, hasHeader, false);
	}

	/**
	 * Return a list of JPA bean read from the given CSV file. Headers are expected.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param resource
	 *            the CSV input resource readable from current class loader.
	 * @param hasHeader
	 *            when <tt>true</tt> the first row contains bean property names.
	 * @param persist
	 *            When <code>true</code> entities are create one by one. Useful to the dependencies.
	 * @return the JPA beans built from CSV input.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toJpa(final Class<T> beanType, final String resource, final boolean hasHeader, final boolean persist) throws IOException {
		return toJpa(beanType, resource, hasHeader, persist, DEFAULT_ENCODING);
	}

	/**
	 * Return a list of JPA bean read from the given CSV file. Headers are expected.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param resource
	 *            the CSV input resource readable from current class loader.
	 * @param hasHeader
	 *            when <tt>true</tt> the first row contains bean property names.
	 * @param persist
	 *            When <code>true</code> entities are create one by one. Useful to the dependencies.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @return the JPA beans built from CSV input.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toJpa(final Class<T> beanType, final String resource, final boolean hasHeader, final boolean persist, final String encoding)
			throws IOException {
		return toJpa(beanType, resource, hasHeader, persist, encoding, null);
	}

	/**
	 * Return a list of JPA bean read from the given CSV file. Headers are expected.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param resource
	 *            the CSV input resource readable from current class loader.
	 * @param hasHeader
	 *            when <tt>true</tt> the first row contains bean property names.
	 * @param persist
	 *            When <code>true</code> entities are create one by one. Useful to the dependencies.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @return the JPA beans built from CSV input.
	 * @param consumer
	 *            Optional Consumer for each entity.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toJpa(final Class<T> beanType, final String resource, final boolean hasHeader, final boolean persist, final String encoding,
			final Consumer<T> consumer) throws IOException {
		Reader input = null;
		try {
			input = new InputStreamReader(new ClassPathResource(resource).getInputStream(), encoding);
			return toJpa(beanType, input, hasHeader, persist, consumer);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	/**
	 * Return a list of JPA bean re ad from the given CSV input.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param input
	 *            the CSV input.
	 * @param hasHeader
	 *            when <tt>true</tt> the first row contains bean property names.
	 * @param persist
	 *            When <code>true</code> entities are create one by one. Useful to the dependencies.
	 * @return the JPA beans built from CSV input.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toJpa(final Class<T> beanType, final Reader input, final boolean hasHeader, final boolean persist) throws IOException {
		return toJpa(beanType, input, hasHeader, persist, null);
	}

	/**
	 * Return a list of JPA bean re ad from the given CSV input.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the JPA bean class.
	 * @param input
	 *            the CSV input.
	 * @param hasHeader
	 *            when <tt>true</tt> the first row contains bean property names.
	 * @param persist
	 *            When <code>true</code> entities are create one by one. Useful to the dependencies.
	 * @param consumer
	 *            Optional Consumer for each entity.
	 * @return the JPA beans built from CSV input.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> toJpa(final Class<T> beanType, final Reader input, final boolean hasHeader, final boolean persist, final Consumer<T> consumer)
			throws IOException {
		final List<T> result = new ArrayList<>();
		final String[] headers;
		final Reader inputProxy;
		if (hasHeader) {
			inputProxy = new BufferedReader(input);
			final String line = ((BufferedReader) inputProxy).readLine();
			if (line == null) {
				// No content means no header, no items.
				return result;
			}

			// Build the headers
			headers = StringUtils.split(line, CsvReader.DEFAULT_SEPARATOR);
		} else {
			inputProxy = input;
			headers = getJpaHeaders(beanType);
		}

		// Read data
		final CsvJpaReader<T> reader = new CsvJpaReader<>(inputProxy, em, beanType, headers);

		// Build all instances
		T order = reader.read();
		while (order != null) {
			result.add(order);
			if (consumer != null) {
				consumer.accept(order);
			}
			if (persist) {
				em.persist(order);
			}
			order = reader.read();
		}

		return result;
	}

	/**
	 * Writes to the given writer the given items in CSV format without header since order is guaranteed by JPA
	 * provider. The generator use only JPA managed properties, so having read and write methods.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param items
	 *            JPA entities to write.
	 * @param beanType
	 *            JPA entity type. Is expected because of dynamic or multiple type of provided items.
	 * @param result
	 *            the target writer.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> void toCsvEntity(final List<T> items, final Class<T> beanType, final Writer result) throws IOException {
		final CsvBeanWriter writer = new CsvBeanWriter(result);
		final String[] headers = getJpaHeaders(beanType);
		for (final T item : items) {
			writer.write(headers, item);
		}
	}

	/**
	 * Field collector.
	 */
	private static class OrderedFieldCallback implements FieldCallback {

		/**
		 * Collected fields.
		 */
		private final List<String> descriptorsOrdered = new ArrayList<>();

		@Override
		public void doWith(final Field field) {
			descriptorsOrdered.add(field.getName());
		}
	}

	/**
	 * Return JPA managed properties.
	 * 
	 * @param <T>
	 *            Bean type.
	 * @param beanType
	 *            the bean type.
	 * @return the headers built from given type.
	 */
	public <T> String[] getJpaHeaders(final Class<T> beanType) {
		// Build descriptor list respecting the declaration order
		final OrderedFieldCallback fieldCallBack = new OrderedFieldCallback();
		ReflectionUtils.doWithFields(beanType, fieldCallBack);
		final List<String> orderedDescriptors = fieldCallBack.descriptorsOrdered;

		// Now filter the properties
		final List<String> descriptorsFiltered = new ArrayList<>();
		final ManagedType<T> managedType = transactionManager.getEntityManagerFactory().getMetamodel().managedType(beanType);
		for (final String propertyDescriptor : orderedDescriptors) {
			for (final Attribute<?, ?> attribute : managedType.getAttributes()) {
				// Match only basic attributes
				if (attribute instanceof SingularAttribute<?, ?> && propertyDescriptor.equals(attribute.getName())) {
					descriptorsFiltered.add(attribute.getName());
					break;
				}
			}
		}

		// Initialize the CSV reader
		return descriptorsFiltered.toArray(new String[descriptorsFiltered.size()]);
	}

	/**
	 * Delete the managed entities. Self referencing rows are set to NULL before the deletion.
	 * 
	 * @param beanTypes
	 *            the ordered set to clean.
	 */
	public void cleanup(final Class<?>... beanTypes) {

		// Clean the data
		for (int i = beanTypes.length; i-- > 0;) {
			final Class<?> entityClass = beanTypes[i];
			final String update = em.getMetamodel().managedType(entityClass).getAttributes().stream().filter(a -> a.getJavaType().equals(entityClass))
					.map(Attribute::getName).map(name -> name + "=NULL").collect(Collectors.joining(", "));
			if (update.length() > 0) {
				// Clear the self referencing rows
				em.createQuery("UPDATE " + entityClass.getName() + " SET " + update).executeUpdate();
			}
			em.createQuery("DELETE FROM " + entityClass.getName()).executeUpdate();
		}
		em.flush();
	}

	/**
	 * Insert the managed entities' table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanTypes
	 *            the ordered bean types (table) set to fill from CSV files.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @return the total inserted table entries.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public int insert(final String csvRoot, final Class<?>[] beanTypes, final String encoding) throws IOException {
		return insert(csvRoot, beanTypes, encoding, null);
	}

	/**
	 * Insert the managed entities' table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanTypes
	 *            the ordered bean types (table) set to fill from CSV files.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @param consumer
	 *            Optional Consumer for each entity.
	 * @return the total inserted table entries.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	@SuppressWarnings("unchecked")
	public int insert(final String csvRoot, final Class<?>[] beanTypes, final String encoding, final Consumer<?> consumer) throws IOException {

		// Replace referential
		int insertedCount = 0;
		for (final Class<?> beanType : beanTypes) {
			insertedCount += insert(csvRoot, (Class<Object>) beanType, encoding, (Consumer<Object>) consumer).size();
		}

		// Free memory
		em.clear();

		return insertedCount;
	}

	/**
	 * Insert the managed entity's table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanType
	 *            The bean class.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @return the total inserted table entries.
	 * @param <T>
	 *            The bean type.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> insert(final String csvRoot, final Class<T> beanType, final String encoding) throws IOException {
		return insert(csvRoot, beanType, encoding, null);
	}

	/**
	 * Insert the managed entity's table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanType
	 *            The bean class.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @return the total inserted table entries.
	 * @param <T>
	 *            The bean type.
	 * @param consumer
	 *            Optional Consumer for each entity.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public <T> List<T> insert(final String csvRoot, final Class<T> beanType, final String encoding, final Consumer<T> consumer) throws IOException {
		InputStreamReader input = null; // / No Java7 feature because of coverage issues
		try {
			input = new InputStreamReader(new ClassPathResourceMultiple(csvRoot, beanType).getInputStream(), encoding);
			return toJpa(beanType, input, true, true, consumer);
		} finally {
			IOUtils.closeQuietly(input);
			em.flush();
		}
	}

	/**
	 * Insert the managed entities' table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanTypes
	 *            the ordered set to refill from CSV files.
	 * @return the total inserted table entries.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public int insert(final String csvRoot, final Class<?>... beanTypes) throws IOException {
		return insert(csvRoot, beanTypes, DEFAULT_ENCODING);
	}

	/**
	 * Delete and insert the managed entities' table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanTypes
	 *            the ordered set to clean, and also to refill from CSV files.
	 * @return the total inserted table entries.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public int reset(final String csvRoot, final Class<?>... beanTypes) throws IOException {
		return reset(csvRoot, beanTypes, DEFAULT_ENCODING);
	}

	/**
	 * Delete and insert the managed entities' table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanTypes
	 *            the ordered set to clean, and also to refill from CSV files.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @return the total inserted table entries.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public int reset(final String csvRoot, final Class<?>[] beanTypes, final String encoding) throws IOException {
		return reset(csvRoot, beanTypes, encoding, null);
	}

	/**
	 * Delete and insert the managed entities' table using CSV file corresponding to the entity name.
	 * 
	 * @param csvRoot
	 *            the root path of CSV resources.
	 * @param beanTypes
	 *            the ordered set to clean, and also to refill from CSV files.
	 * @param encoding
	 *            the encoding used to read the CSV resources.
	 * @param consumer
	 *            Optional Consumer for each entity.
	 * @return the total inserted table entries.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public int reset(final String csvRoot, final Class<?>[] beanTypes, final String encoding, final Consumer<?> consumer) throws IOException {
		cleanup(beanTypes);
		return insert(csvRoot, beanTypes, encoding, consumer);
	}

}
