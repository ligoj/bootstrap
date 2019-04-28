/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.DateUtils;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;

/**
 * Test of {@link AbstractDataGeneratorTest}
 */
class TestAbstractDataGeneratorTest extends AbstractDataGeneratorTest {

	@BeforeAll
    static void setApplicationTimeZone() {
		// Fix UTC time zone for this test
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Test
	void testSetApplicationContext() {
		setApplicationContext(Mockito.mock(ApplicationContext.class));
	}

	@Test
	void testGetInt() {
		Assertions.assertSame(0, getInt(""));
	}

	@Test
	void testGetDate() {
		final var instance = DateUtils.newCalendar();
		instance.set(1970, 0, 1, 0, 0, 0);
		instance.set(Calendar.MILLISECOND, 0);
		Assertions.assertEquals(instance.getTime(), getDate(""));
	}

	@Test
	void testGetDate2() {
		final var instance = DateUtils.newCalendar();
		instance.set(1970, 0, 1, 0, 0, 0);
		instance.set(Calendar.MILLISECOND, 0);
		Assertions.assertEquals(instance.getTime(), getDate(1970, 1, 1));
	}

	@Test
	void testNow() {
		now(); // Dummy test for coverage
	}

	@Test
	void testGetDateInt() {
		Assertions.assertEquals(0, getDate(0).getTime());
	}

	@Test
	void testGetCharInt() {
		Assertions.assertSame('A', getChar(0));
	}

	@Test
	void testGetCharString() {
		Assertions.assertSame('A', getChar(""));
	}

	@Test
	void testGetDoubleRange() {
		Assertions.assertEquals(0.0, getDouble("", 0, 0), 0.0001);
	}

	@Test
	void testGetIntRange() {
		Assertions.assertSame(0, getInt("", 0, 0));
	}

	@Test
	void testGetItem() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItem("", list));
	}

	@Test
	void testGetItem2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItem(0, list));
	}

	@Test
	void testGetItemArrray() {
		Assertions.assertEquals("test", getItem("", new String[] { "test" }));
	}

	@Test
	void testGetItemOrNull() {
		final List<String> list = new ArrayList<>();
		Assertions.assertNotSame(0, getItemOrNull("", list));
	}

	@Test
	void testGetItemOrNull2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItemOrNull("1", list));
	}

	@Test
	void testGetItemOrNullInt() {
		final List<String> list = new ArrayList<>();
		Assertions.assertNull(getItemOrNull(0, list));
	}

	@Test
	void testGetItemOrNullInt2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItemOrNull(1, list));
	}

	@Test
	void testGetItemOrNullArray() {
		Assertions.assertNull(getItemOrNull("", new String[0]));
	}

	@Test
	void testGetItemOrNullArray2() {
		Assertions.assertEquals("test", getItemOrNull("1", new String[] { "test" }));
	}

	@Test
	void testEnum() {
		Assertions.assertEquals(HttpMethod.GET, getEnum("", HttpMethod.class));
	}

	@Test
	void testGetItems() {
		Assertions.assertEquals(new ArrayList<String>(), getItems("", new ArrayList<String>(), 0, 0));
	}

	@Test
	void testGetItems2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals(list, getItems("", list, 1, 1));
	}

	@Test
	void testGetItems3() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		list.add("test2");
		final List<String> result = new ArrayList<>();
		result.add("test2");
		result.add("test");
		Assertions.assertEquals(result, getItems("1", list, 1, 7));
	}

	@Test
	void testGetBoolean() {
		Assertions.assertTrue(getBoolean(1));
	}

	@Test
	void testGetBooleanString() {
		Assertions.assertFalse(getBoolean(""));
	}

	@Test
	void testNewUriInfo() {
		Assertions.assertEquals("search-1", newUriInfo("search-1").getQueryParameters().getFirst("search[value]"));
	}

	@Test
	void testCoverageSingleton() throws ReflectiveOperationException {
		coverageSingleton(Singleton.class);
	}

	static class Singleton {
		private Singleton() {
			// Utility class
		}
	}
}
