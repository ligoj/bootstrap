/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Test class of {@link StringListConverter}.
 */
class StringListConverterTest {

	@Test
	void convertToDatabaseColumn() {
		Assertions.assertEquals("a;b", new StringListConverter().convertToDatabaseColumn(List.of("a", "b")));
	}

	@Test
	void convertToDatabaseColumnEmpty() {
		Assertions.assertNull(new StringListConverter().convertToDatabaseColumn(List.of()));
	}

	@Test
	void convertToDatabaseColumnNull() {
		Assertions.assertNull(new StringListConverter().convertToDatabaseColumn(null));
	}

	@Test
	void convertToEntityAttribute() {
		Assertions.assertEquals(List.of("a", "b"), new StringListConverter().convertToEntityAttribute("a;b"));
	}

	@Test
	void convertToEntityAttributeNull() {
		Assertions.assertEquals(List.of(), new StringListConverter().convertToEntityAttribute(null));
	}

	@Test
	void convertToEntityAttributeEmpty() {
		Assertions.assertEquals(List.of(), new StringListConverter().convertToEntityAttribute(""));
	}
}
