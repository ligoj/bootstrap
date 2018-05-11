/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.ligoj.bootstrap.core.DateUtils;
import org.ligoj.bootstrap.core.SpringUtils;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;

/**
 * Provides convenient methods to generate pseudo-generated data. Assuming a given salt, generated data will be always
 * the same, but seems to changes randomly with the given inputs.
 */
public abstract class AbstractDataGeneratorTest extends AbstractTest implements ApplicationContextAware {

	/**
	 * Random instance.
	 */
	protected Random random = new SecureRandom();

	/**
	 * The {@link ApplicationContext} that was injected into this test instance via
	 * {@link #setApplicationContext(ApplicationContext)}.
	 */
	protected ApplicationContext applicationContext;

	/**
	 * Set the {@link ApplicationContext} to be used by this test instance, provided via {@link ApplicationContextAware}
	 * semantics.
	 */
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Restore original Spring application context
	 */
	@AfterEach
	@BeforeEach
	public void restoreApplicationContext() {
		if (applicationContext != null) {
			// This test was running in a Spring context, restore the shared context
			SpringUtils.setSharedApplicationContext(applicationContext);
		}
	}

	/**
	 * Return a salt from a string.
	 *
	 * @param salt
	 *            Any string.
	 * @return a salt from a string.
	 */
	protected int getInt(final String salt) {
		return Math.abs(Math.max(Integer.MIN_VALUE, salt.hashCode()));
	}

	/**
	 * Return a date from a salt.
	 *
	 * @param salt
	 *            Any string.
	 * @return a date from a salt.
	 */
	protected Date getDate(final String salt) {
		return getDate(getInt(salt));
	}

	/**
	 * Get a date
	 *
	 * @param year
	 *            year
	 * @param month
	 *            1 based (January = 1)
	 * @param day
	 *            day
	 * @param hour
	 *            hour
	 * @param minute
	 *            minute
	 * @param second
	 *            second
	 * @return Date
	 */
	protected Date getDate(final int year, final int month, final int day, final int hour, final int minute, final int second) {
		final Calendar calendar = DateUtils.newCalendar();
		calendar.clear();
		calendar.set(year, month - 1, day, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * Get a date
	 *
	 * @param year
	 *            year
	 * @param month
	 *            1 based (January = 1)
	 * @param day
	 *            day
	 * @return Date
	 */
	protected Date getDate(final int year, final int month, final int day) {
		return getDate(year, month, day, 0, 0, 0);
	}

	/**
	 * Return current date, from UTC time-zone.
	 *
	 * @return current date, from UTC time-zone.
	 */
	protected Date now() {
		return DateUtils.newCalendar().getTime();
	}

	/**
	 * Return a date from a salt.
	 *
	 * @param salt
	 *            Any integer.
	 * @return a date from a salt.
	 */
	protected Date getDate(final int salt) {
		final Calendar manufacturingDate = DateUtils.newCalendar();
		manufacturingDate.set(Calendar.YEAR, getInt(salt, 1970, 1988));
		manufacturingDate.set(Calendar.MONTH, getInt(salt, 0, 11));
		manufacturingDate.set(Calendar.DAY_OF_MONTH, getInt(salt, 1, 28));
		manufacturingDate.set(Calendar.HOUR_OF_DAY, 0);
		manufacturingDate.set(Calendar.MINUTE, 0);
		manufacturingDate.set(Calendar.SECOND, 0);
		manufacturingDate.set(Calendar.MILLISECOND, 0);
		return manufacturingDate.getTime();
	}

	protected char getChar(final int anyInt) {
		return (char) ('A' + getInt(anyInt, 0, 26));
	}

	protected char getChar(final String anyInt) {
		return (char) ('A' + getInt(anyInt, 0, 26));
	}

	/**
	 * Return a salt from a string.
	 *
	 * @param salt
	 *            Any string.
	 * @param lower
	 *            lower value.
	 * @param upper
	 *            upper value. Excluded value.
	 * @return a salt from a string.
	 */
	protected double getDouble(final String salt, final int lower, final int upper) {
		random.setSeed(salt.hashCode());
		return random.nextDouble() * (upper - lower) + lower;
	}

	/**
	 * Return a salt from a string.
	 *
	 * @param salt
	 *            Any string.
	 * @param lower
	 *            lower value.
	 * @param upper
	 *            upper value. Excluded value.
	 * @return a salt from a string.
	 */
	protected int getInt(final String salt, final int lower, final int upper) {
		return getInt(getInt(salt), lower, upper);
	}

	/**
	 * Return one of given items.
	 *
	 * @param salt
	 *            Any string used as salt.
	 * @param items
	 *            source items.
	 * @return one of given items.
	 * @param <T>
	 *            the type of the items.
	 */
	protected <T> T getItem(final String salt, final List<T> items) {
		return items.get(getInt(salt, 0, items.size()));
	}

	/**
	 * Return one of given items.
	 *
	 * @param salt
	 *            Any string used as salt.
	 * @param items
	 *            source items.
	 * @return one of given items or <tt>null</tt>.
	 * @param <T>
	 *            the type of the items.
	 */
	protected <T> T getItemOrNull(final String salt, final List<T> items) {
		final int index = getInt(salt, 0, items.size() + 1) - 1;
		if (index == -1) {
			return null;
		}
		return items.get(index);
	}

	/**
	 * Read and return lines of given text file.
	 *
	 * @param textFileName
	 *            the file to read.
	 * @return lines read from the given text file.
	 */
	protected List<String> readList(final String textFileName) throws IOException {
		try (InputStream input = new ClassPathResource(textFileName).getInputStream()) {
			return new ArrayList<>(new HashSet<>(IOUtils.readLines(input, StandardCharsets.UTF_8)));
		}
	}

	/**
	 * Return one of given items.
	 *
	 * @param salt
	 *            Any string used as salt.
	 * @param items
	 *            source items.
	 * @return one of given items.
	 * @param <T>
	 *            the type of the items.
	 */
	protected <T> T getItem(final int salt, final List<T> items) {
		return items.get(getInt(salt, 0, items.size()));
	}

	/**
	 * Return one of given items.
	 *
	 * @param salt
	 *            Any string used as salt.
	 * @param items
	 *            source items.
	 * @return one of given items.
	 * @param <T>
	 *            the type of the items.
	 */
	@SafeVarargs
	protected final <T> T getItem(final String salt, final T... items) {
		return items[getInt(salt, 0, items.length)];
	}

	/**
	 * Return one of given items.
	 *
	 * @param salt
	 *            Any string used as salt.
	 * @param items
	 *            source items.
	 * @return one of given items or <tt>null</tt>.
	 * @param <T>
	 *            the type of the items.
	 */
	protected <T> T getItemOrNull(final int salt, final List<T> items) {
		final int index = getInt(salt, 0, items.size() + 1) - 1;
		if (index == -1) {
			return null;
		}
		return items.get(index);
	}

	/**
	 * Return one of given items.
	 *
	 * @param salt
	 *            Any string used as salt.
	 * @param items
	 *            source items.
	 * @return one of given items or <tt>null</tt>.
	 * @param <T>
	 *            the type of the items.
	 */
	@SafeVarargs
	protected final <T> T getItemOrNull(final String salt, final T... items) {
		final int index = getInt(salt, 0, items.length + 1) - 1;
		if (index == -1) {
			return null;
		}
		return items[index];
	}

	/**
	 * Return a enumeration literal from a salt.
	 *
	 * @param salt
	 *            Any string used as salt.
	 * @param enumClass
	 *            enumeration class.
	 * @return a enumeration literal from a salt.
	 * @param <T>
	 *            the type of the enum.
	 */
	protected <T> T getEnum(final String salt, final Class<T> enumClass) {
		return getItem(salt, enumClass.getEnumConstants());
	}

	/**
	 * Return a salt from a integer.
	 *
	 * @param salt
	 *            Any integer.
	 * @param lower
	 *            lower value.
	 * @param upper
	 *            upper value. Excluded value.
	 * @return a salt from a integer.
	 */
	protected int getInt(final int salt, final int lower, final int upper) {
		if (lower >= upper) {
			return lower;
		}
		return Math.abs(salt) % (upper - lower) + lower;
	}

	/**
	 * Return a subset of items.
	 *
	 * @param salt
	 *            any string used as salt.
	 * @param items
	 *            source items.
	 * @param lower
	 *            lower value.
	 * @param upper
	 *            upper value. Excluded value.
	 * @param <T>
	 *            the type of the items.
	 * @return a subset of items.
	 */
	protected <T> List<T> getItems(final String salt, final List<T> items, final int lower, final int upper) {
		final List<T> result = new ArrayList<>(upper);
		final int size = Math.min(items.size(), getInt(salt, lower, upper));
		final Set<Integer> added = new HashSet<>(upper);
		int counter = size;
		int saltIncrement = 0;
		while (counter != 0) {
			final int index = getInt(salt + counter + saltIncrement++, 0, items.size());
			if (added.add(Integer.valueOf(index))) {
				counter--;
				result.add(items.get(index));
			}
		}
		return result;
	}

	/**
	 * Return a boolean from a string.
	 *
	 * @param salt
	 *            Any string.
	 * @return a boolean from a string.
	 */
	protected Boolean getBoolean(final String salt) {
		return getBoolean(getInt(salt));
	}

	/**
	 * Return a boolean from a number.
	 *
	 * @param salt
	 *            Any integer.
	 * @return a boolean from a number.
	 */
	protected Boolean getBoolean(final int salt) {
		return getInt(salt, 0, 2) == 1;
	}

	/**
	 * Return a new mocked {@link UriInfo} instance.
	 *
	 * @return a new mocked {@link UriInfo} instance.
	 */
	protected UriInfo newUriInfo() {
		final UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MetadataMap<String, String>());
		return uriInfo;
	}

	/**
	 * Return a new mocked {@link UriInfo} instance.
	 *
	 * @param search
	 *            The search value as query parameter.
	 * @return a new mocked {@link UriInfo} instance.
	 */
	protected UriInfo newUriInfo(final String search) {
		final UriInfo uriInfo = newUriInfo();
		uriInfo.getQueryParameters().putSingle("search[value]", search);
		return uriInfo;
	}

	/**
	 * Check and execute coverage test on an utility class.
	 *
	 * @param singletonClass
	 *            The utility class
	 */
	protected <S> void coverageSingleton(final Class<S> singletonClass)
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final Constructor<S> constructor = singletonClass.getDeclaredConstructor();
		Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}
}
