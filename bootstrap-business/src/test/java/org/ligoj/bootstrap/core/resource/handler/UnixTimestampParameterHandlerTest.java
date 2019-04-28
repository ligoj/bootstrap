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
class UnixTimestampParameterHandlerTest extends AbstractDataGeneratorTest {

	@Test
	void dateNull() {
		Assertions.assertNull(new UnixTimestampParameterHandler().getConverter(Date.class, null, null).fromString(null));
	}

	@Test
	void dateOther() {
		Assertions.assertNull(new UnixTimestampParameterHandler().getConverter(String.class, null, null));
	}

	@Test
	void date() {
		final var today = new Date();
		Assertions.assertEquals(today,
				new UnixTimestampParameterHandler().getConverter(Date.class, null, null).fromString(String.valueOf(today.getTime())));
	}

	@Test
	void localDate() {
		Assertions.assertEquals(LocalDate.of(2016, 8, 15), new UnixTimestampParameterHandler().getConverter(LocalDate.class, null, null)
				.fromString(String.valueOf(getDate(2016, 8, 15).getTime())));
	}

	@Test
	void dateToString() {
		final var today = new Date();
		Assertions.assertEquals(String.valueOf(today.getTime()),
				new UnixTimestampParameterHandler().getConverter(Date.class, null, null).toString(today));
	}
}
