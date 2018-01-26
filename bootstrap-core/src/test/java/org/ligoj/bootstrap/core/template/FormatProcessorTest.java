package org.ligoj.bootstrap.core.template;

import java.text.ParseException;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.model.system.SystemUser;

/**
 * Test class of {@link FormatProcessor}
 */
public class FormatProcessorTest {

	/**
	 * Simple date format of static context.
	 */
	@Test
	public void testGetValue() throws ParseException {
		final Deque<Object> contextData = new LinkedList<>();
		final SystemUser systemUser = new SystemUser();
		contextData.add(systemUser);
		final FastDateFormat df = FastDateFormat.getInstance("yyyy/MM/dd", null, null);
		Assertions.assertEquals("2014/05/30", new FormatProcessor<>(df, DateUtils.parseDate("2014/05/30", "yyyy/MM/dd")).getValue(contextData));
	}

	/**
	 * Simple date format of dynamic context.
	 */
	@Test
	public void testGetItemValue() throws ParseException {
		final Deque<Object> contextData = new LinkedList<>();
		contextData.add(DateUtils.parseDate("2014/05/30", "yyyy/MM/dd"));
		final FastDateFormat df = FastDateFormat.getInstance("yyyy/MM/dd", null, null);
		Assertions.assertEquals("2014/05/30", new FormatProcessor<>(df).getValue(contextData));
	}

}
