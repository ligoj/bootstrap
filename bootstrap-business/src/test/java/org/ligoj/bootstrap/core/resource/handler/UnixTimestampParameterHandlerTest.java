package org.ligoj.bootstrap.core.resource.handler;

import java.time.LocalDate;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import org.ligoj.bootstrap.AbstractDataGeneratorTest;

/**
 * Test class of {@link UnixTimestampParameterHandler}
 */
public class UnixTimestampParameterHandlerTest extends AbstractDataGeneratorTest {

	@Test
	public void dateNull() {
		Assert.assertNull(new UnixTimestampParameterHandler().getConverter(Date.class, null, null).fromString(null));
	}

	@Test
	public void dateOther() {
		Assert.assertEquals(null, new UnixTimestampParameterHandler().getConverter(String.class, null, null));
	}

	@Test
	public void date() {
		final Date today = new Date();
		Assert.assertEquals(today,
				new UnixTimestampParameterHandler().getConverter(Date.class, null, null).fromString(String.valueOf(today.getTime())));
	}

	@Test
	public void localDate() {
		Assert.assertEquals(LocalDate.of(2016, 8, 15), new UnixTimestampParameterHandler().getConverter(LocalDate.class, null, null)
				.fromString(String.valueOf(getDate(2016, 8, 15).getTime())));
	}

	@Test
	public void dateToString() {
		final Date today = new Date();
		Assert.assertEquals(String.valueOf(today.getTime()),
				new UnixTimestampParameterHandler().getConverter(Date.class, null, null).toString(today));
	}
}
