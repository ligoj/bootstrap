package org.ligoj.bootstrap.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * JSon write/read bench and tests.
 */
@Slf4j
public class JSonMapperTest {

	private ObjectMapper jackSonMapper;

	public static final int ITERATION = 100000;

	@BeforeEach
	public void initializeMapper() throws IOException {
		jackSonMapper = new ObjectMapper();
		warmup();

	}

	private void warmup() throws IOException {
		for (int i = ITERATION; i-- > 0;) {
			readObject("\"value\"", String.class);
		}

	}

	@Data
	public static class TestDate {
		private Date date;
	}

	@Test
	public void testReadDate() throws IOException {
		TestDate lastObject = new TestDate();
		final long start = System.currentTimeMillis();
		for (int i = ITERATION; i-- > 0;) {
			lastObject = readObject("{\"date\":5}", TestDate.class);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getDate());
		Assertions.assertEquals(5L, lastObject.getDate().getTime());
		log.info("read-date\t" + (end - start));
	}

	@Test
	public void testWriteDate() throws IOException {
		final TestDate lastObject = new TestDate();
		lastObject.setDate(new Date());
		String lastValue = null;
		final long start = System.currentTimeMillis();
		for (int i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestDate.class), lastObject);
		log.info("write-date\t" + (end - start));
	}

	@Data
	public static class TestLong {
		private Long numberLong;
		private Long numberLong2;
		private Long numberLong3;
		private Long numberLong4;
		private Long numberLong5;
		private Long numberLong6;
	}

	@Test
	public void testReadLong() throws IOException {
		TestLong lastObject = new TestLong();
		final long start = System.currentTimeMillis();
		for (int i = ITERATION * 10; i-- > 0;) {
			lastObject = readObject("{\"numberLong\":6,\"numberLong2\":6,\"numberLong3\":6,\"numberLong4\":6,\"numberLong5\":6,\"numberLong6\":6}",
					TestLong.class);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getNumberLong());
		Assertions.assertEquals(6L, lastObject.getNumberLong().longValue());
		log.info("read-long\t" + (end - start));
	}

	@Test
	public void testWriteLong() throws IOException {
		final TestLong lastObject = new TestLong();
		lastObject.setNumberLong(6L);
		String lastValue = null;
		final long start = System.currentTimeMillis();
		for (int i = ITERATION * 10; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestLong.class), lastObject);
		log.info("write-long\t" + (end - start));
	}

	@Data
	public static class TestStringArray {
		private List<String> array;
	}

	@Test
	public void testReadStringList() throws IOException {
		TestStringArray lastObject = new TestStringArray();
		final StringBuilder valueBuffer = new StringBuilder("{\"array\":[");
		for (int i = 100; i-- > 0;) {
			if (i != 99) {
				valueBuffer.append(',');
			}
			valueBuffer.append("\"value" + i + "\"");
		}
		valueBuffer.append("]}");
		final String value = valueBuffer.toString();

		final long start = System.currentTimeMillis();
		for (int i = ITERATION; i-- > 0;) {
			lastObject = readObject(value, TestStringArray.class);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getArray());
		Assertions.assertEquals(100, lastObject.getArray().size());
		log.info("read-string-list\t" + (end - start));
	}

	@Test
	public void testWriteStringList() throws IOException {
		final TestStringArray lastObject = new TestStringArray();
		final List<String> array = new ArrayList<>();
		for (int i = 100; i-- > 0;) {
			array.add("value" + i);
		}
		lastObject.setArray(array);
		String lastValue = null;
		final long start = System.currentTimeMillis();
		for (int i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestStringArray.class), lastObject);
		log.info("write-string-list\t" + (end - start));
	}

	@Data
	public static class TestGenericArray {
		private List<TestLong> array;
	}

	@Test
	public void testReadGenericArray() throws IOException {
		TestGenericArray lastObject = new TestGenericArray();
		final StringBuilder valueBuffer = new StringBuilder("{\"array\":[");
		for (int i = 100; i-- > 0;) {
			if (i != 99) {
				valueBuffer.append(',');
			}
			valueBuffer.append("{\"numberLong\":6}");
		}
		valueBuffer.append("]}");
		final String value = valueBuffer.toString();

		final long start = System.currentTimeMillis();
		for (int i = ITERATION; i-- > 0;) {
			lastObject = readObject(value, TestGenericArray.class);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getArray());
		Assertions.assertEquals(100, lastObject.getArray().size());
		log.info("read-generic-list\t" + (end - start));
	}

	@Test
	public void testWriteGenericArray() throws IOException {
		final TestGenericArray lastObject = new TestGenericArray();
		final List<TestLong> array = new ArrayList<>();
		for (int i = 100; i-- > 0;) {
			final TestLong object = new TestLong();
			object.setNumberLong(6L);
			array.add(object);
		}
		lastObject.setArray(array);
		String lastValue = null;
		final long start = System.currentTimeMillis();
		for (int i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestGenericArray.class), lastObject);
		log.info("write-generic-list\t" + (end - start));
	}

	@Data
	public static class TestGenericObjectArray<K, V> {
		private K key;
		private V value;
	}

	@SuppressWarnings({ "cast", "unchecked" })
	@Test
	public void testReadGenericObjectArray() throws IOException {
		final StringBuilder valueBuffer = new StringBuilder("[");
		for (int i = 20; i-- > 0;) {
			if (i != 19) {
				valueBuffer.append(',');
			}
			valueBuffer.append("{\"key\":6,\"value\":\"Val\"}");
		}
		valueBuffer.append("]");
		final String value = valueBuffer.toString();

		final long start = System.currentTimeMillis();
		List<Map<Object, Object>> lastObject = new ArrayList<>();
		for (int i = ITERATION; i-- > 0;) {
			lastObject = (List<Map<Object, Object>>) readObject(value, List.class);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertEquals(20, lastObject.size());
		final Map<Object, Object> object = lastObject.get(0);
		Assertions.assertNotNull(object);
		Assertions.assertEquals("Val", object.get("value"));
		Assertions.assertEquals(6, ((Integer) object.get("key")).intValue());
		log.info("read-generic-object-array\t" + (end - start));
	}

	@Test
	public void testWriteGenericObjectArray() throws IOException {
		@SuppressWarnings({ "cast", "unchecked" })
		final TestGenericObjectArray<Integer, String>[] lastObject = (TestGenericObjectArray<Integer, String>[]) new TestGenericObjectArray[20];
		for (int i = 20; i-- > 0;) {
			lastObject[i] = new TestGenericObjectArray<>();
			lastObject[i].setKey(6);
			lastObject[i].setValue("Val");
		}
		String lastValue = null;
		final long start = System.currentTimeMillis();
		for (int i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final long end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertArrayEquals(lastObject, readObject(lastValue, TestGenericObjectArray[].class));
		log.info("write-generic-object-array\t" + (end - start));
	}

	private <T> T readObject(final String value, final Class<T> clazz) throws IOException {
		return jackSonMapper.readValue(value, clazz);
	}

	private String writeValue(final Object object) throws IOException {
		return jackSonMapper.writeValueAsString(object);
	}
}
