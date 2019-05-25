/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core;

import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Check dare utilities of DateUtils.
 */
class DateUtilsTest {

	@Test
	void newCalendar() {
		Assertions.assertEquals(DateUtils.newCalendar().getTimeZone(), DateUtils.getApplicationTimeZone());
	}

	@Test
	void setApplicationTimeZone() {
		var timeZone = DateUtils.getApplicationTimeZone();
		try {
			DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("GMT"));
			Assertions.assertEquals("GMT", DateUtils.getApplicationTimeZone().getID());
		} finally {
			DateUtils.setApplicationTimeZone(timeZone);
		}
	}

	@Test
	void testCoverage() throws ReflectiveOperationException {
		var constructor = DateUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}

}
