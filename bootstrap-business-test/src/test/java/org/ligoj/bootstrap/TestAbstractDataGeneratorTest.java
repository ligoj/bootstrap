package org.ligoj.bootstrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
public class TestAbstractDataGeneratorTest extends AbstractDataGeneratorTest {

	@BeforeAll
	public static void setApplicationTimeZone() {
		// Fix UTC time zone for this test
		DateUtils.setApplicationTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Test
	public void testSetApplicationContext() {
		setApplicationContext(Mockito.mock(ApplicationContext.class));
	}

	@Test
	public void testGetInt() {
		Assertions.assertSame(0, getInt(""));
	}

	@Test
	public void testGetDate() {
		final Calendar instance = DateUtils.newCalendar();
		instance.set(1970, 0, 1, 0, 0, 0);
		instance.set(Calendar.MILLISECOND, 0);
		Assertions.assertEquals(instance.getTime(), getDate(""));
	}

	@Test
	public void testGetDate2() {
		final Calendar instance = DateUtils.newCalendar();
		instance.set(1970, 0, 1, 0, 0, 0);
		instance.set(Calendar.MILLISECOND, 0);
		Assertions.assertEquals(instance.getTime(), getDate(1970, 1, 1));
	}

	@Test
	public void testNow() {
		now(); // Dummy test for coverage
	}

	@Test
	public void testGetDateInt() {
		Assertions.assertEquals(0, getDate(0).getTime());
	}

	@Test
	public void testGetCharInt() {
		Assertions.assertSame('A', getChar(0));
	}

	@Test
	public void testGetCharString() {
		Assertions.assertSame('A', getChar(""));
	}

	@Test
	public void testGetDoubleRange() {
		Assertions.assertEquals(0.0, getDouble("", 0, 0), 0.0001);
	}

	@Test
	public void testGetIntRange() {
		Assertions.assertSame(0, getInt("", 0, 0));
	}

	@Test
	public void testGetItem() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItem("", list));
	}

	@Test
	public void testGetItem2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItem(0, list));
	}

	@Test
	public void testGetItemArrray() {
		Assertions.assertEquals("test", getItem("", new String[] { "test" }));
	}

	@Test
	public void testGetItemOrNull() {
		final List<String> list = new ArrayList<>();
		Assertions.assertNotSame(0, getItemOrNull("", list));
	}

	@Test
	public void testGetItemOrNull2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItemOrNull("1", list));
	}

	@Test
	public void testGetItemOrNullInt() {
		final List<String> list = new ArrayList<>();
		Assertions.assertNull(getItemOrNull(0, list));
	}

	@Test
	public void testGetItemOrNullInt2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals("test", getItemOrNull(1, list));
	}

	@Test
	public void testGetItemOrNullArray() {
		Assertions.assertNull(getItemOrNull("", new String[0]));
	}

	@Test
	public void testGetItemOrNullArray2() {
		Assertions.assertEquals("test", getItemOrNull("1", new String[] { "test" }));
	}

	@Test
	public void testEnum() {
		Assertions.assertEquals(HttpMethod.GET, getEnum("", HttpMethod.class));
	}

	@Test
	public void testReadList() throws IOException {
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			readList("none");
		});
	}

	@Test
	public void testReadList2() throws IOException {
		readList("log4j2.json");
	}

	@Test
	public void testGetItems() {
		Assertions.assertEquals(new ArrayList<String>(), getItems("", new ArrayList<String>(), 0, 0));
	}

	@Test
	public void testGetItems2() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		Assertions.assertEquals(list, getItems("", list, 1, 1));
	}

	@Test
	public void testGetItems3() {
		final List<String> list = new ArrayList<>();
		list.add("test");
		list.add("test2");
		final List<String> result = new ArrayList<>();
		result.add("test2");
		result.add("test");
		Assertions.assertEquals(result, getItems("1", list, 1, 7));
	}

	@Test
	public void testGetBoolean() {
		Assertions.assertTrue(getBoolean(1));
	}

	@Test
	public void testGetBooleanString() {
		Assertions.assertFalse(getBoolean(""));
	}

	@Test
	public void testNewUriInfo() {
		Assertions.assertEquals("search-1", newUriInfo("search-1").getQueryParameters().getFirst("search[value]"));
	}

	@Test
	public void testCoverageSingleton()
			throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		coverageSingleton(Singleton.class);
	}

	public static class Singleton {
		private Singleton() {
			// Utility class
		}
	}
}
