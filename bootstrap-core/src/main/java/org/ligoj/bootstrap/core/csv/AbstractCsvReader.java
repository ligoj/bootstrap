package org.ligoj.bootstrap.core.csv;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.GeneratedValue;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.joda.time.DateTime;
import org.ligoj.bootstrap.core.DateUtils;
import org.ligoj.bootstrap.core.resource.TechnicalException;

/**
 * CSV reader implementation based on Camel implementation (see
 * "BindyCsvDataFormat") where some issues have been fixed.
 * 
 * @param <T>
 *            Bean type.
 */
public abstract class AbstractCsvReader<T> {

	/**
	 * Bean utility.
	 */
	protected final BeanUtilsBean beanUtilsBean;

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
	 * @see <a href="https://docs.oracle.com/javase/9/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
	 */
	public static final String DATE_PATTERN_ISO2 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	/**
	 * Date patterns together.
	 */
	protected static final String[] DATE_PATTERNS = { DATE_PATTERN_HMS, DATE_PATTERN_HM, DATE_PATTERN, DATE_PATTERN_EN_HMS,
			DATE_PATTERN_EN_HM, DATE_PATTERN_EN, DATE_PATTERN_HMS.replace('/', '-'), DATE_PATTERN_HM.replace('/', '-'),
			DATE_PATTERN.replace('/', '-'), DATE_PATTERN_EN_HMS.replace('/', '-'), DATE_PATTERN_EN_HM.replace('/', '-'),
			DATE_PATTERN_EN.replace('/', '-'), DATE_PATTERN_ISO, DATE_PATTERN_ISO2 };

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
	 * All fields constructor.
	 * 
	 * @param reader
	 *            Input reader.
	 * @param beanType
	 *            Class of bean to build.
	 * @param headers
	 *            Headers, an ordered property list. Header with <code>null</code>
	 *            or empty name will skip the corresponding column.
	 */
	public AbstractCsvReader(final Reader reader, final Class<T> beanType, final String... headers) {
		this.csvReader = new CsvReader(reader);
		this.headers = headers;
		this.clazz = beanType;

		// Put default date patterns
		final DateConverter dateConverter = new DateConverter();
		dateConverter.setPatterns(DATE_PATTERNS);
		dateConverter.setLocale(Locale.FRANCE);
		dateConverter.setTimeZone(DateUtils.getApplicationTimeZone());
		ConvertUtils.register(dateConverter, Date.class);

		// Joda DateTime support
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("unchecked")
			@Override
			public <D> D convert(final Class<D> type, final Object value) {
				return (D) new DateTime(dateConverter.convert(Date.class, value));
			}
		}, DateTime.class);

		// Java 8 LocalDate support
		ConvertUtils.register(new Converter() {

			@SuppressWarnings("unchecked")
			@Override
			public <D> D convert(final Class<D> type, final Object value) {
				return (D) Instant.ofEpochMilli(dateConverter.convert(Date.class, value).getTime())
						.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toLocalDate();
			}
		}, LocalDate.class);

		this.beanUtilsBean = BeanUtilsBean.getInstance();
	}

	/**
	 * Return a bean read from the reader.
	 * 
	 * @return the read bean.
	 * @throws IOException
	 *             Read issue occurred.
	 */
	public T read() throws IOException {
		return build(csvReader.read());
	}

	/**
	 * Return a bean read from the reader.
	 * 
	 * @param values
	 *            the property values.
	 * @return the bean built with values.
	 */
	protected T build(final List<String> values) {
		if (values.isEmpty()) {
			return null;
		}
		if (headers.length < values.size()) {
			throw new TechnicalException(String.format("Too much values for type %s. Expected : %d, got : %d : %s", clazz.getName(),
					headers.length, values.size(), values));
		}

		// Build the instance
		try {
			final T bean = clazz.getDeclaredConstructor().newInstance();

			// Fill the instance
			fillInstance(bean, values);

			// Bean is completed
			return bean;
		} catch (final Exception e) {
			throw new TechnicalException("Unable to build an object of type : " + clazz, e);
		}
	}

	/**
	 * Fill the given bean.
	 * 
	 * @param bean
	 *            The target bean.
	 * @param values
	 *            The raw {@link String} values to set to the bean.
	 */
	protected void fillInstance(final T bean, final List<String> values)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		int index = 0;
		for (final String property : headers) {
			if (index >= values.size()) {
				// Trailing null data are ignored
				break;
			}

			// Read only data of mapped column
			if (StringUtils.isNotBlank(property)) {

				// Read the mapped column value
				final String rawValue = values.get(index);
				if (rawValue.length() > 0) {
					setProperty(bean, property, rawValue);
				}
			}
			index++;
		}
	}

	/**
	 * Set the property to the given bean.
	 * 
	 * @param bean
	 *            the target bean.
	 * @param property
	 *            the bean property to set.
	 * @param rawValue
	 *            the raw value to set.
	 */
	protected abstract void setProperty(T bean, String property, String rawValue)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

	/**
	 * Return the field from the given class.
	 * 
	 * @param beanType
	 *            Class of bean to build.
	 * @param property
	 *            the bean property to get.
	 * @return the {@link Field} of this property.
	 */
	protected Field getField(final Class<?> beanType, final String property) {
		final Field field = FieldUtils.getField(beanType, property, true);
		if (field == null) {
			throw new TechnicalException("Unknown property " + property + " in class " + beanType.getName());
		}
		return field;
	}

	/**
	 * Add a value to a map.
	 * 
	 * @param bean
	 *            the target bean.
	 * @param property
	 *            the bean property to set. Must be a {@link Map}
	 * @param key
	 *            the key of the {@link Map} property
	 * @param rawValue
	 *            the raw value to put in the {@link Map}.
	 */
	protected void setMapProperty(final T bean, final String property, final String key, final String rawValue)
			throws IllegalAccessException {
		final Field mapField = getField(clazz, property);

		// Get/initialize the Map
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) mapField.get(bean);
		if (map == null) {
			map = new LinkedHashMap<>();
			mapField.set(bean, map);
		}

		// Check duplicate entries
		if (map.put(key, rawValue) != null) {
			throw new TechnicalException(
					"Duplicate map entry key='" + key + "' for map property " + property + " in class " + bean.getClass().getName());
		}
	}

	/**
	 * Manage simple value with map management.
	 * 
	 * @param bean
	 *            the target bean.
	 * @param property
	 *            the bean property to set.
	 * @param rawValue
	 *            the raw value to set.
	 */
	protected void setSimpleProperty(final T bean, final String property, final String rawValue)
			throws IllegalAccessException, InvocationTargetException {
		final int mapIndex = property.indexOf('$');
		if (mapIndex == -1) {
			setSimpleRawProperty(bean, property, rawValue);
		} else {
			setMapProperty(bean, property.substring(0, mapIndex), property.substring(mapIndex + 1), rawValue);
		}
	}

	/**
	 * Manage simple value.
	 * 
	 * @param bean
	 *            the target bean.
	 * @param property
	 *            the bean property to set.
	 * @param rawValue
	 *            the raw value to set.
	 * @param <E>
	 *            Enumeration type.
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Enum<E>> void setSimpleRawProperty(final T bean, final String property, final String rawValue)
			throws IllegalAccessException, InvocationTargetException {
		final Field field = getField(clazz, property);

		// Update the property
		if (field.getAnnotation(GeneratedValue.class) == null) {
			if (field.getType().isEnum()) {
				// Ignore case of Enum name
				final Class<E> enumClass = (Class<E>) field.getType();
				beanUtilsBean.setProperty(bean, property, Enum.valueOf(enumClass,
						EnumUtils.getEnumMap(enumClass).keySet().stream().filter(rawValue::equalsIgnoreCase).findFirst().orElse(rawValue)));
			} else {
				beanUtilsBean.setProperty(bean, property, rawValue);
			}
		}

	}
}