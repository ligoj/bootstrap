package org.ligoj.bootstrap.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * Check dare utilities of DateUtils.
 */
public class DateUtilsTest {

	@Test
	public void newCalendar() {
		Assert.assertEquals(DateUtils.newCalendar().getTimeZone(), DateUtils.getApplicationTimeZone());
	}

	@Test
	public void setApplicationTimeZone() {
		TimeZone timeZone = DateUtils.getApplicationTimeZone();
		try {
			DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("GMT"));
			Assert.assertEquals("GMT", DateUtils.getApplicationTimeZone().getID());
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
