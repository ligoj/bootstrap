package org.ligoj.bootstrap.core.dao.csv;

import org.ligoj.bootstrap.core.csv.AbstractCsvReader;

import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV reader implementation based on Camel implementation (see BindyCsvDataFormat) where some issues have been fixed.
 * 
 * @param <T>
 *            Bean type.
 */
public class CsvJpaReader<T> extends AbstractCsvReader<T> {

	/**
	 * optional entity manager
	 */
	private final EntityManager em;

	/**
	 * Cache of fetched foreign keys, property value based.
	 */
	private final Map<Class<?>, Map<String, Object>> foreignCache = new HashMap<>();

	/**
	 * Cache of fetched foreign keys, index value based.
	 */
	private final Map<Class<?>, List<?>> foreignCacheRows = new HashMap<>();

	/**
	 * Simple override.
	 * 
	 * @param reader
	 *            Input reader.
	 * @param clazz
	 *            Class of bean to build.
	 * @param headers
	 *            Headers, an ordered property list.
	 * @param em
	 *            The {@link EntityManager} used to get properties and foreign keys.
	 */
	public CsvJpaReader(final Reader reader, final EntityManager em, final Class<T> clazz, final String... headers) {
		super(reader, clazz, headers);
		this.em = em;
	}

	@Override
	protected void setProperty(final T bean, final String property, final String rawValue) throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		final int fkeyIndex = property.indexOf('.');
		if (fkeyIndex == -1) {
			setSimpleProperty(bean, property, rawValue);
		} else {
			setForeignProperty(bean, property, rawValue, fkeyIndex);
		}
	}

	/**
	 * Manage foreign key.
	 */
	private void setForeignProperty(final T bean, final String property, final String rawValue, final int fkeyIndex)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final String masterPropertyName = property.substring(0, fkeyIndex);
		final Field jpaField = getField(clazz, masterPropertyName);
		String propertyName = property.substring(fkeyIndex + 1);
		final Object foreignEntity;
		if (propertyName.charAt(propertyName.length() - 1) == '!') {
			propertyName = propertyName.substring(0, propertyName.length() - 1);
			foreignEntity = readFromEm(rawValue, jpaField, propertyName);
		} else {
			foreignEntity = readFromCache(rawValue, jpaField, propertyName);
		}
		if (foreignEntity == null) {
			throw new IllegalArgumentException("Missing foreign key " + jpaField.getDeclaringClass().getSimpleName() + "#" + masterPropertyName + "."
					+ propertyName + " = " + rawValue);
		}
		beanUtilsBean.setProperty(bean, masterPropertyName, foreignEntity);
	}

	/**
	 * Read from entity manager.
	 */
	private Object readFromEm(final String rawValue, final Field jpaField, final String propertyName) {
		// On by one foreign key
		final List<?> resultList;
		if (isRowNumber(jpaField, propertyName)) {
			// search referenced entity with a filter on row number
			resultList = em.createQuery("FROM " + jpaField.getType().getName()).setFirstResult(Integer.parseInt(rawValue) - 1).setMaxResults(1)
					.getResultList();
		} else {
			// search referenced entity with a filter on propertyName
			resultList = em
					.createQuery("FROM " + jpaField.getType().getName() + " WHERE " + propertyName + " like '" + rawValue + "'", jpaField.getType())
					.setMaxResults(1).getResultList();

		}

		return resultList.stream().findFirst().orElse(null);
	}

	/**
	 * Read from already read entities.
	 */
	private Object readFromCache(final String rawValue, final Field jpaField, final String propertyName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// Special fetching mode
		if (isRowNumber(jpaField, propertyName)) {
			// search referenced entity with a filter on row number
			return readFromRowNumberCache(rawValue, jpaField);
		}

		// search referenced entity with a filter based on natural join
		return readFromJoinCache(rawValue, jpaField, propertyName);
	}

	/**
	 * Read from already read row index
	 */
	private Object readFromJoinCache(final String rawValue, final Field jpaField, final String propertyName) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		ensureCache(jpaField, propertyName);
		return foreignCache.get(jpaField.getType()).get(rawValue);
	}

	/**
	 * Read from database and try to match the value of column to match then entity.
	 */
	private Object readFromRowNumberCache(final String rawValue, final Field jpaField) {
		final int index = Integer.parseInt(rawValue);
		if (!foreignCacheRows.containsKey(jpaField.getType())) {
			foreignCacheRows.put(jpaField.getType(), readAll(jpaField));
		}

		if (index <= foreignCacheRows.get(jpaField.getType()).size() && index > 0) {
			return foreignCacheRows.get(jpaField.getType()).get(index - 1);
		}
		return null;
	}

	/**
	 * Initialize or update cache.
	 */
	private void ensureCache(final Field jpaField, final String propertyName) throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (!foreignCache.containsKey(jpaField.getType()) || jpaField.getType() == clazz) {
			foreignCache.put(jpaField.getType(), buildMap(readAll(jpaField), propertyName));
		}
	}

	/**
	 * Indicates this field is using a auto generated key or not.
	 */
	private boolean isRowNumber(final Field jpaField, final String propertyName) {
		return getField(jpaField.getType(), propertyName).getAnnotation(GeneratedValue.class) != null;
	}

	private List<?> readAll(final Field jpaField) {
		return em.createQuery("FROM " + jpaField.getType().getName()).getResultList();
	}

	/**
	 * Return a map where key is the foreign key and value is the entity.
	 */
	private Map<String, Object> buildMap(final List<?> list, final String property) throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		final Map<String, Object> result = new HashMap<>();
		for (final Object item : list) {
			result.put(String.valueOf(beanUtilsBean.getProperty(item, property)), item);
		}
		return result;
	}
}