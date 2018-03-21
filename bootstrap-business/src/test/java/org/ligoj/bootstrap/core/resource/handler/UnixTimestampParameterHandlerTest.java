/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.AbstractDataGeneratorTest;

/**
 * Test class of {@link UnixTimestampParameterHandler}
 */
public class UnixTimestampParameterHandlerTest extends AbstractDataGeneratorTest {

	@Test
	public void dateNull() {
		Assertions.assertNull(new UnixTimestampParameterHandler().getConverter(Date.class, null, null).fromString(null));
	}

	@Test
	public void dateOther() {
		Assertions.assertEquals(null, new UnixTimestampParameterHandler().getConverter(String.class, null, null));
	}

	@Test
	public void date() {
		final Date today = new Date();
		Assertions.assertEquals(today,
				new UnixTimestampParameterHandler().getConverter(Date.class, null, null).fromString(String.valueOf(today.getTime())));
	}

	@Test
	public void localDate() {
		Assertions.assertEquals(LocalDate.of(2016, 8, 15), new UnixTimestampParameterHandler().getConverter(LocalDate.class, null, null)
				.fromString(String.valueOf(getDate(2016, 8, 15).getTime())));
	}

	@Test
	public void dateToString() {
		final Date today = new Date();
		Assertions.assertEquals(String.valueOf(today.getTime()),
				new UnixTimestampParameterHandler().getConverter(Date.class, null, null).toString(today));
	}
}
