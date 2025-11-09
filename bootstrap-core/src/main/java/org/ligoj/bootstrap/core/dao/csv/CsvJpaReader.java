/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.dao.csv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.ligoj.bootstrap.core.csv.AbstractCsvReader;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * CSV reader implementation based on Camel implementation (see BindyCsvDataFormat) where some issues have been fixed.
 *
 * @param <T> Bean type.
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
	 * @param reader  Input reader.
	 * @param clazz   Class of bean to build.
	 * @param headers Headers, an ordered property list.
	 * @param em      The {@link EntityManager} used to get properties and foreign keys.
	 */
	public CsvJpaReader(final Reader reader, final EntityManager em, final Class<T> clazz, final String... headers) {
		super(reader, clazz, headers);
		this.em = em;
	}

	@Override
	protected void setForeignProperty(final T bean, final String fqname, final String rawValue, final int keyIndex) {
		final var name = fqname.substring(0, keyIndex);
		final var field = getField(clazz, name);
		final var keyName = fqname.substring(keyIndex + 1);

		// Collection management
		if (field.getType().isAssignableFrom(Set.class)) {
			// Set support
			beanUtilsBean.setProperty(bean, name, newCollection(rawValue, name, field, keyName, new HashSet<>()));
		} else if (field.getType().isAssignableFrom(List.class)) {
			// List support
			beanUtilsBean.setProperty(bean, name, newCollection(rawValue, name, field, keyName, new ArrayList<>()));
		} else {
			// Simple property
			beanUtilsBean.setProperty(bean, name, getForeignProperty(rawValue, name, field,
					TypeUtils.getRawType(field.getGenericType(), bean.getClass()), keyName));
		}
	}

	private Object getForeignProperty(final String rawValue, final String name, final Field field, final Class<?> type,
	                                  final String fkName) {
		final Object foreignEntity;
		if (fkName.charAt(fkName.length() - 1) == '!') {
			foreignEntity = readFromEm(rawValue, type, fkName.substring(0, fkName.length() - 1));
		} else {
			foreignEntity = readFromCache(rawValue, type, fkName);
		}
		if (foreignEntity == null) {
			throw new IllegalArgumentException("Missing foreign key " + field.getDeclaringClass().getSimpleName() + "#"
					+ name + "." + Strings.CS.removeEnd(fkName, "!") + " = " + rawValue);
		}
		return foreignEntity;
	}

	private Collection<Object> newCollection(final String rawValue, final String masterPropertyName, final Field field,
	                                         String propertyName, final Collection<Object> arrayList) {
		final var generic = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
		for (final var item : rawValue.split(",")) {
			arrayList.add(getForeignProperty(item, masterPropertyName, field, generic, propertyName));
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
	private Object readFromCache(final String rawValue, final Class<?> type, final String propertyName) {
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
	private Object readFromJoinCache(final String rawValue, final Class<?> type, final String propertyName) {
		ensureCache(type, propertyName);
		return foreignCache.get(type).get(rawValue);
	}

	/**
	 * Read from database and try to match the value of column to match then entity.
	 */
	private Object readFromRowNumberCache(final String rawValue, final Class<?> type) {
		final var index = Integer.parseInt(rawValue);
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
	private void ensureCache(final Class<?> type, final String propertyName) {
		if (!foreignCache.containsKey(type) || type == clazz) {
			foreignCache.put(type, buildMap(readAll(type), propertyName));
		}
	}

	/**
	 * Indicates this field is using an auto generated key or not.
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
	private Map<String, Object> buildMap(final List<?> list, final String property) {
		final var result = new HashMap<String, Object>();
		for (final var item : list) {
			final var value = beanUtilsBean.getProperty(item, property);
			if (value != null) {
				result.put(String.valueOf(value), item);
			}
		}
		return result;
	}
}