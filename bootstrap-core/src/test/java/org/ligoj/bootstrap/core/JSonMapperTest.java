/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
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
class JSonMapperTest {

	private ObjectMapper jackSonMapper;

	private static final int ITERATION = 100000;

	@BeforeEach
    void initializeMapper() throws IOException {
		jackSonMapper = new ObjectMapper();
		warmup();

	}

	private void warmup() throws IOException {
		for (var i = ITERATION; i-- > 0;) {
			readObject("\"value\"", String.class);
		}

	}

	@Data
    static class TestDate {
		private Date date;
	}

	@Test
    void testReadDate() throws IOException {
        var lastObject = new TestDate();
		final var start = System.currentTimeMillis();
		for (var i = ITERATION; i-- > 0;) {
			lastObject = readObject("{\"date\":5}", TestDate.class);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getDate());
		Assertions.assertEquals(5L, lastObject.getDate().getTime());
		log.info("read-date\t" + (end - start));
	}

	@Test
    void testWriteDate() throws IOException {
		final var lastObject = new TestDate();
		lastObject.setDate(new Date());
		String lastValue = null;
		final var start = System.currentTimeMillis();
		for (var i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestDate.class), lastObject);
		log.info("write-date\t" + (end - start));
	}

	@Data
    static class TestLong {
		private Long numberLong;
		private Long numberLong2;
		private Long numberLong3;
		private Long numberLong4;
		private Long numberLong5;
		private Long numberLong6;
	}

	@Test
    void testReadLong() throws IOException {
        var lastObject = new TestLong();
		final var start = System.currentTimeMillis();
		for (var i = ITERATION * 10; i-- > 0;) {
			lastObject = readObject("{\"numberLong\":6,\"numberLong2\":6,\"numberLong3\":6,\"numberLong4\":6,\"numberLong5\":6,\"numberLong6\":6}",
					TestLong.class);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getNumberLong());
		Assertions.assertEquals(6L, lastObject.getNumberLong().longValue());
		log.info("read-long\t" + (end - start));
	}

	@Test
    void testWriteLong() throws IOException {
		final var lastObject = new TestLong();
		lastObject.setNumberLong(6L);
		String lastValue = null;
		final var start = System.currentTimeMillis();
		for (var i = ITERATION * 10; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestLong.class), lastObject);
		log.info("write-long\t" + (end - start));
	}

	@Data
    static class TestStringArray {
		private List<String> array;
	}

	@Test
    void testReadStringList() throws IOException {
        var lastObject = new TestStringArray();
		final var valueBuffer = new StringBuilder("{\"array\":[");
		for (var i = 100; i-- > 0;) {
			if (i != 99) {
				valueBuffer.append(',');
			}
			valueBuffer.append("\"value").append(i).append('\"');
		}
		valueBuffer.append("]}");
		final var value = valueBuffer.toString();

		final var start = System.currentTimeMillis();
		for (var i = ITERATION; i-- > 0;) {
			lastObject = readObject(value, TestStringArray.class);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getArray());
		Assertions.assertEquals(100, lastObject.getArray().size());
		log.info("read-string-list\t" + (end - start));
	}

	@Test
    void testWriteStringList() throws IOException {
		final var lastObject = new TestStringArray();
		final var array = new ArrayList<String>();
		for (var i = 100; i-- > 0;) {
			array.add("value" + i);
		}
		lastObject.setArray(array);
		String lastValue = null;
		final var start = System.currentTimeMillis();
		for (var i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestStringArray.class), lastObject);
		log.info("write-string-list\t" + (end - start));
	}

	@Data
    static class TestGenericArray {
		private List<TestLong> array;
	}

	@Test
    void testReadGenericArray() throws IOException {
        var lastObject = new TestGenericArray();
		final var valueBuffer = new StringBuilder("{\"array\":[");
		for (var i = 100; i-- > 0;) {
			if (i != 99) {
				valueBuffer.append(',');
			}
			valueBuffer.append("{\"numberLong\":6}");
		}
		valueBuffer.append("]}");
		final var value = valueBuffer.toString();

		final var start = System.currentTimeMillis();
		for (var i = ITERATION; i-- > 0;) {
			lastObject = readObject(value, TestGenericArray.class);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertNotNull(lastObject.getArray());
		Assertions.assertEquals(100, lastObject.getArray().size());
		log.info("read-generic-list\t" + (end - start));
	}

	@Test
    void testWriteGenericArray() throws IOException {
		final var lastObject = new TestGenericArray();
		final var array = new ArrayList<TestLong>();
		for (var i = 100; i-- > 0;) {
			final var object = new TestLong();
			object.setNumberLong(6L);
			array.add(object);
		}
		lastObject.setArray(array);
		String lastValue = null;
		final var start = System.currentTimeMillis();
		for (var i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastValue);
		Assertions.assertEquals(readObject(lastValue, TestGenericArray.class), lastObject);
		log.info("write-generic-list\t" + (end - start));
	}

	@Data
    static class TestGenericObjectArray<K, V> {
		private K key;
		private V value;
	}

	@SuppressWarnings("unchecked")
	@Test
    void testReadGenericObjectArray() throws IOException {
		final var valueBuffer = new StringBuilder("[");
		for (var i = 20; i-- > 0;) {
			if (i != 19) {
				valueBuffer.append(',');
			}
			valueBuffer.append("{\"key\":6,\"value\":\"Val\"}");
		}
		valueBuffer.append("]");
		final var value = valueBuffer.toString();

		final var start = System.currentTimeMillis();
		List<Map<Object, Object>> lastObject = new ArrayList<>();
		for (var i = ITERATION; i-- > 0;) {
			lastObject = readObject(value, List.class);
		}
		final var end = System.currentTimeMillis();
		Assertions.assertNotNull(lastObject);
		Assertions.assertEquals(20, lastObject.size());
		final var object = lastObject.get(0);
		Assertions.assertNotNull(object);
		Assertions.assertEquals("Val", object.get("value"));
		Assertions.assertEquals(6, ((Integer) object.get("key")).intValue());
		log.info("read-generic-object-array\t" + (end - start));
	}

	@Test
    void testWriteGenericObjectArray() throws IOException {
		@SuppressWarnings("unchecked")
		final TestGenericObjectArray<Integer, String>[] lastObject = new TestGenericObjectArray[20];
		for (var i = 20; i-- > 0;) {
			lastObject[i] = new TestGenericObjectArray<>();
			lastObject[i].setKey(6);
			lastObject[i].setValue("Val");
		}
		String lastValue = null;
		final var start = System.currentTimeMillis();
		for (var i = ITERATION; i-- > 0;) {
			lastValue = writeValue(lastObject);
		}
		final var end = System.currentTimeMillis();
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
