package org.ligoj.bootstrap.core.dao.csv;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;

import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.csv.AbstractCsvReader;

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
	protected void setProperty(final T bean, final String property, final String rawValue)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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

		// Collection management
		if (Set.class.isAssignableFrom(jpaField.getType())) {
			// Set support
			beanUtilsBean.setProperty(bean, masterPropertyName,
					newCollection(rawValue, masterPropertyName, jpaField, propertyName, new HashSet<>()));
		} else if (List.class.isAssignableFrom(jpaField.getType()) || Collection.class.equals(jpaField.getType())) {
			// List support
			beanUtilsBean.setProperty(bean, masterPropertyName,
					newCollection(rawValue, masterPropertyName, jpaField, propertyName, new ArrayList<>()));
		} else {
			// Simple property
			beanUtilsBean.setProperty(bean, masterPropertyName,
					getForeignProperty(rawValue, masterPropertyName, jpaField, jpaField.getType(), propertyName));
		}
	}

	private Object getForeignProperty(final String rawValue, final String masterPropertyName, final Field jpaField,
			final Class<?> type, final String propertyName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Object foreignEntity;
		if (propertyName.charAt(propertyName.length() - 1) == '!') {
			foreignEntity = readFromEm(rawValue, type, propertyName.substring(0, propertyName.length() - 1));
		} else {
			foreignEntity = readFromCache(rawValue, type, propertyName);
		}
		if (foreignEntity == null) {
			throw new IllegalArgumentException("Missing foreign key " + jpaField.getDeclaringClass().getSimpleName()
					+ "#" + masterPropertyName + "." + StringUtils.removeEnd(propertyName, "!") + " = " + rawValue);
		}
		return foreignEntity;
	}

	private Collection<Object> newCollection(final String rawValue, final String masterPropertyName,
			final Field jpaField, String propertyName, final Collection<Object> arrayList)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Class<?> genericType = (Class<?>) ((ParameterizedType) jpaField.getGenericType())
				.getActualTypeArguments()[0];
		for (final String item : rawValue.split(",")) {
			arrayList.add(getForeignProperty(item, masterPropertyName, jpaField, genericType, propertyName));
		}
		return arrayList;
	}

	/**
	 * Read from entity manager.
	 */
	private Object readFromEm(final String rawValue, final Class<?> type, final String propertyName) {
		// On by one foreign key
		final List<?> resultList;
		if (isRowNumber(type, propertyName)) {
			// search referenced entity with a filter on row number
			resultList = em.createQuery(from(type)).setFirstResult(Integer.parseInt(rawValue) - 1).setMaxResults(1)
					.getResultList();
		} else {
			// search referenced entity with a filter on propertyName
			resultList = em
					.createQuery(String.format("%s WHERE %s LIKE '%s'", from(type), propertyName, rawValue), type)
					.setMaxResults(1).getResultList();

		}

		return resultList.stream().findFirst().orElse(null);
	}

	/**
	 * Return a "FROM" query based on the given filed type.
	 */
	private String from(final Class<?> type) {
		return "FROM " + type.getName();
	}

	/**
	 * Read from already read entities.
	 */
	private Object readFromCache(final String rawValue, final Class<?> type, final String propertyName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// Special fetching mode
		if (isRowNumber(type, propertyName)) {
			// search referenced entity with a filter on row number
			return readFromRowNumberCache(rawValue, type);
		}

		// search referenced entity with a filter based on natural join
		return readFromJoinCache(rawValue, type, propertyName);
	}

	/**
	 * Read from already read row index
	 */
	private Object readFromJoinCache(final String rawValue, final Class<?> type, final String propertyName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		ensureCache(type, propertyName);
		return foreignCache.get(type).get(rawValue);
	}

	/**
	 * Read from database and try to match the value of column to match then entity.
	 */
	private Object readFromRowNumberCache(final String rawValue, final Class<?> type) {
		final int index = Integer.parseInt(rawValue);
		if (!foreignCacheRows.containsKey(type)) {
			foreignCacheRows.put(type, readAll(type));
		}

		if (index <= foreignCacheRows.get(type).size() && index > 0) {
			return foreignCacheRows.get(type).get(index - 1);
		}
		return null;
	}

	/**
	 * Initialize or update cache.
	 */
	private void ensureCache(final Class<?> type, final String propertyName)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (!foreignCache.containsKey(type) || type == clazz) {
			foreignCache.put(type, buildMap(readAll(type), propertyName));
		}
	}

	/**
	 * Indicates this field is using a auto generated key or not.
	 */
	private boolean isRowNumber(final Class<?> type, final String propertyName) {
		return getField(type, propertyName).getAnnotation(GeneratedValue.class) != null;
	}

	private List<?> readAll(final Class<?> type) {
		return em.createQuery(from(type)).getResultList();
	}

	/**
	 * Return a map where key is the foreign key and value is the entity.
	 */
	private Map<String, Object> buildMap(final List<?> list, final String property)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Map<String, Object> result = new HashMap<>();
		for (final Object item : list) {
			final Object value = beanUtilsBean.getProperty(item, property);
			if (value != null) {
				result.put(String.valueOf(value), item);
			}
		}
		return result;
	}
}