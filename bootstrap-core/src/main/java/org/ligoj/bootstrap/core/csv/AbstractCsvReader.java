/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import jakarta.persistence.GeneratedValue;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.ligoj.bootstrap.core.DateUtils;
import org.ligoj.bootstrap.core.resource.TechnicalException;

import jodd.bean.BeanUtil;
import jodd.typeconverter.TypeConversionException;
import jodd.typeconverter.TypeConverter;
import jodd.typeconverter.TypeConverterManager;
import jodd.typeconverter.impl.DateConverter;

/**
 * CSV reader implementation based on Camel implementation where some issues have been fixed.
 *
 * @param <T> Bean type.
 */
public abstract class AbstractCsvReader<T> {

	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String HH_MM = " HH:mm";
	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String HH_MM_SS = HH_MM + ":ss";
	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String DATE_PATTERN = "dd/MM/yyyy";
	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String DATE_PATTERN_HMS = DATE_PATTERN + HH_MM_SS;
	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String DATE_PATTERN_HM = DATE_PATTERN + HH_MM;
	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String DATE_PATTERN_EN = "yyyy/MM/dd";
	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String DATE_PATTERN_EN_HMS = DATE_PATTERN_EN + HH_MM_SS;
	/**
	 * Accepted date patterns, see orders.
	 */
	public static final String DATE_PATTERN_EN_HM = DATE_PATTERN_EN + HH_MM;

	/**
	 * ISO8601 format
	 */
	public static final String DATE_PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";

	/**
	 * ISO8601 format with milliseconds - 2018-01-01T00:00:00.000+01:00
	 *
	 * @see <a href="https://docs.oracle.com/javase/9/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
	 */
	public static final String DATE_PATTERN_ISO2 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	/**
	 * Date patterns together.
	 */
	protected static final String[] DATE_PATTERNS = {DATE_PATTERN_HMS, DATE_PATTERN_HM, DATE_PATTERN,
			DATE_PATTERN_EN_HMS, DATE_PATTERN_EN_HM, DATE_PATTERN_EN, DATE_PATTERN_HMS.replace('/', '-'),
			DATE_PATTERN_HM.replace('/', '-'), DATE_PATTERN.replace('/', '-'), DATE_PATTERN_EN_HMS.replace('/', '-'),
			DATE_PATTERN_EN_HM.replace('/', '-'), DATE_PATTERN_EN.replace('/', '-'), DATE_PATTERN_ISO,
			DATE_PATTERN_ISO2};

	/**
	 * The ordered headers used to build the target bean.
	 */
	protected final String[] headers;

	/**
	 * The instance type to build.
	 */
	protected final Class<T> clazz;

	/**
	 * CSV raw data reader.
	 */
	protected final CsvReader csvReader;

	/**
	 * Bean utility.
	 */
	protected final BeanUtil beanUtilsBean = BeanUtil.declared;

	/**
	 * Local {@link Field} cache.
	 */
	protected final Map<Class<?>, Map<String, Field>> fields = new WeakHashMap<>();

	/**
	 * All fields constructor.
	 *
	 * @param reader   Input reader.
	 * @param beanType Class of bean to build.
	 * @param headers  Headers, an ordered property list. Header with <code>null</code> or empty name will skip the
	 *                 corresponding column. Column values are also trimmed.
	 */
	protected AbstractCsvReader(final Reader reader, final Class<T> beanType, final String... headers) {
		this.csvReader = new CsvReader(reader);
		this.headers = StringUtils.stripAll(headers);
		this.clazz = beanType;

		// Put default date patterns

		final var dateConverter = new DateConverter();
		final TypeConverter<Date> dateJConverter = value -> {
			try {
				return dateConverter.convert(value);
			} catch (DateTimeParseException | TypeConversionException tce) {
				for (final var pattern : DATE_PATTERNS) {
					final var format = new SimpleDateFormat(pattern);
					format.setTimeZone(DateUtils.getApplicationTimeZone());
					format.setLenient(false);
					try {
						return format.parse((String) value);
					} catch (ParseException dfe) {
						// Ignore
					}
				}
				throw new DateTimeParseException("No date format found", String.valueOf(value), 0);
			}
		};
		TypeConverterManager.get().register(Date.class, dateJConverter);

		// Java 8 LocalDate support
		TypeConverterManager.get().register(LocalDate.class,
				value -> Instant.ofEpochMilli(dateJConverter.convert(value).getTime())
						.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toLocalDate());
		TypeConverterManager.get().register(Instant.class,
				value -> dateJConverter.convert(value).toInstant());
	}

	/**
	 * Return a bean read from the reader.
	 *
	 * @return the read bean.
	 * @throws IOException Read issue occurred.
	 */
	public T read() throws IOException {
		return read(null);
	}

	/**
	 * Return a bean read from the reader.
	 *
	 * @param setter Optional setter for raw properties.
	 * @return the read bean.
	 * @throws IOException Read issue occurred.
	 */
	public T read(final TriConsumer<T, String, String> setter) throws IOException {
		return build(csvReader.read(), setter);
	}

	/**
	 * Return a bean read from the reader.
	 *
	 * @param setter Optional setter for raw properties.
	 * @param values the property values.
	 * @return the bean built with values.
	 */
	protected T build(final List<String> values, final TriConsumer<T, String, String> setter) {
		if (values.isEmpty()) {
			return null;
		}
		if (headers.length < values.size()) {
			throw new TechnicalException(String.format("Too much values for type %s. Expected : %d, got : %d : %s",
					clazz.getName(), headers.length, values.size(), values));
		}

		// Build the instance
		try {
			final var bean = clazz.getDeclaredConstructor().newInstance();

			// Fill the instance
			fillInstance(bean, values, setter);

			// Bean is completed
			return bean;
		} catch (final Exception e) {
			throw new TechnicalException("Unable to build an object of type : " + clazz, e);
		}
	}

	/**
	 * Fill the given bean.
	 *
	 * @param bean   The target bean.
	 * @param values The raw {@link String} values to set to the bean.
	 * @param setter Optional setter for raw properties.
	 * @throws ReflectiveOperationException When bean cannot be built with reflection.
	 */
	protected void fillInstance(final T bean, final List<String> values, final TriConsumer<T, String, String> setter)
			throws ReflectiveOperationException {
		var index = 0;
		for (final var property : headers) {
			if (index >= values.size()) {
				// Trailing null data are ignored
				break;
			}

			// Read only data of mapped column
			if (!property.isEmpty()) {

				// Read the mapped column value
				final var rawValue = values.get(index);
				if (!rawValue.isEmpty()) {
					setProperty(bean, property, rawValue, setter);
				}
			}
			index++;
		}
	}

	/**
	 * Set the property to the given bean.
	 *
	 * @param bean     the target bean.
	 * @param property the bean property to set.
	 * @param rawValue the raw value to set.
	 * @param setter   Optional setter for raw properties.
	 * @throws ReflectiveOperationException When bean cannot be built with reflection.
	 */
	protected void setProperty(final T bean, final String property, final String rawValue,
			final TriConsumer<T, String, String> setter) throws ReflectiveOperationException {
		if (setter == null) {
			final var keyIndex = property.indexOf('.');
			if (keyIndex == -1) {
				setSimpleProperty(bean, property, rawValue);
			} else {
				setForeignProperty(bean, property, rawValue, keyIndex);
			}
		} else {
			setter.accept(bean, property, rawValue);
		}
	}

	/**
	 * Manage foreign key.
	 *
	 * @param bean     Target bean.
	 * @param property Target property.
	 * @param rawValue Source value.
	 * @param keyIndex Foreign key index.
	 */
	protected void setForeignProperty(final T bean, final String property, final String rawValue, final int keyIndex) {
		throw new TechnicalException("Foreign key management is not supported in bean mode");
	}

	/**
	 * Return the field from the given class.
	 *
	 * @param beanType Class of bean to build.
	 * @param property the bean property to get.
	 * @return the {@link Field} of this property.
	 */
	protected Field getField(final Class<?> beanType, final String property) {
		final var field = fields.computeIfAbsent(beanType, c -> new HashMap<>()).computeIfAbsent(property,
				p -> FieldUtils.getField(beanType, p, true));
		if (field == null) {
			throw new TechnicalException("Unknown property " + property + " in class " + beanType.getName());
		}
		return field;
	}

	/**
	 * Add a value to a map.
	 *
	 * @param bean     the target bean.
	 * @param property the bean property to set. Must be a {@link Map}
	 * @param key      the key of the {@link Map} property
	 * @param rawValue the raw value to put in the {@link Map}.
	 * @throws IllegalAccessException if this {@code Field} object is enforcing Java language access control and the
	 *                                underlying field is inaccessible.
	 */
	protected void setMapProperty(final T bean, final String property, final String key, final String rawValue)
			throws IllegalAccessException {
		final var mapField = getField(clazz, property);

		// Get/initialize the Map
		@SuppressWarnings("unchecked")
		var map = (Map<String, String>) mapField.get(bean);
		if (map == null) {
			map = new LinkedHashMap<>();
			mapField.set(bean, map);
		}

		// Check duplicate entries
		if (map.put(key, rawValue) != null) {
			throw new TechnicalException("Duplicate map entry key='" + key + "' for map property " + property
					+ " in class " + bean.getClass().getName());
		}
	}

	/**
	 * Manage simple value with map management.
	 *
	 * @param bean     the target bean.
	 * @param property the bean property to set.
	 * @param rawValue the raw value to set.
	 * @throws IllegalAccessException When bean cannot be built with reflection.
	 */
	private void setSimpleProperty(final T bean, final String property, final String rawValue)
			throws IllegalAccessException {
		final var mapIndex = property.indexOf('$');
		if (mapIndex == -1) {
			setSimpleRawProperty(bean, property, rawValue);
		} else {
			setMapProperty(bean, property.substring(0, mapIndex), property.substring(mapIndex + 1), rawValue);
		}
	}

	/**
	 * Manage simple value.
	 *
	 * @param bean     the target bean.
	 * @param property the bean property to set.
	 * @param rawValue the raw value to set.
	 * @param <E>      Enumeration type.
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Enum<E>> void setSimpleRawProperty(final T bean, final String property,
			final String rawValue) {
		final var field = getField(clazz, property);

		// Update the property
		if (field.getAnnotation(GeneratedValue.class) == null) {
			if (field.getType().isEnum()) {
				// Ignore case of Enum name
				final var enumClass = (Class<E>) field.getType();
				beanUtilsBean.setProperty(bean, property, Enum.valueOf(enumClass, EnumUtils.getEnumMap(enumClass)
						.keySet().stream().filter(rawValue::equalsIgnoreCase).findFirst().orElse(rawValue)));
			} else if (field.getType().isAssignableFrom(Set.class)) {
				// Set support
				beanUtilsBean.setProperty(bean, property, newCollection(rawValue, field, new HashSet<>()));
			} else if (field.getType().isAssignableFrom(List.class)) {
				// List support
				beanUtilsBean.setProperty(bean, property, newCollection(rawValue, field, new ArrayList<>()));
			} else if (Number.class
					.isAssignableFrom(org.apache.commons.lang3.ClassUtils.primitiveToWrapper(field.getType()))) {
				// Simple numeric property
				beanUtilsBean.setProperty(bean, property, Strings.CS.replace(rawValue, " ", "").replace(',', '.'));
			} else {
				// Simple property
				beanUtilsBean.setProperty(bean, property, rawValue);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <E extends Enum<E>> Collection<Object> newCollection(final String rawValue, final Field field,
			final Collection<Object> result) {
		final var generic = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
		for (final var item : rawValue.split(",")) {
			if (generic.isEnum()) {
				// Ignore case of Enum name
				final var enumClass = (Class<E>) generic;
				result.add(Enum.valueOf(enumClass, EnumUtils.getEnumMap(enumClass).keySet().stream()
						.filter(rawValue::equalsIgnoreCase).findFirst().orElse(item)));
			} else {
				result.add(TypeConverterManager.get().convertType(item, generic));
			}
		}
		return result;
	}
}