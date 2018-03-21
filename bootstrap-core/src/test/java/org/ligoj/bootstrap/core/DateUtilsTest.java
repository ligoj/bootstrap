/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Check dare utilities of DateUtils.
 */
public class DateUtilsTest {

	@Test
	public void newCalendar() {
		Assertions.assertEquals(DateUtils.newCalendar().getTimeZone(), DateUtils.getApplicationTimeZone());
	}

	@Test
	public void setApplicationTimeZone() {
		TimeZone timeZone = DateUtils.getApplicationTimeZone();
		try {
			DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("GMT"));
			Assertions.assertEquals("GMT", DateUtils.getApplicationTimeZone().getID());
		} finally {
			DateUtils.setApplicationTimeZone(timeZone);
		}
	}

	@Test
	public void testCoverage()
			throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<DateUtils> constructor = DateUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}

}
