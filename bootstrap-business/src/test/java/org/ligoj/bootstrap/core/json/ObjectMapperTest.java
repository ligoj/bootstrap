/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.AbstractDataGeneratorTest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Test class of {@link ObjectMapperTrim}
 */
public class ObjectMapperTest extends AbstractDataGeneratorTest {

	private com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapperTrim();
	private com.fasterxml.jackson.databind.ObjectMapper vanillaMapper = new com.fasterxml.jackson.databind.ObjectMapper();

	@Test
	public void serializationEnum() throws JsonProcessingException {
		Assertions.assertEquals("\"accepted\"", mapper.writeValueAsString(Status.ACCEPTED));
		Assertions.assertEquals("\"ACCEPTED\"", vanillaMapper.writeValueAsString(Status.ACCEPTED));
	}

	@Test
	public void serializationDate() throws IOException {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2).getTime()),
				mapper.writeValueAsString(getDate(2016, 8, 2)));
	}

	@Test
	public void serializationLocalDateTime() throws IOException {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2, 12, 54, 32).getTime()),
				mapper.writeValueAsString(LocalDateTime.of(2016, 8, 2, 12, 54, 32)));
	}

	@Test
	public void serializationLocalDate() throws IOException {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2).getTime()),
				mapper.writeValueAsString(LocalDate.of(2016, 8, 2)));
	}

	@Test
	public void deserializationDate() throws IOException {
		Assertions.assertEquals(getDate(2016, 8, 2),
				mapper.readValue(String.valueOf(getDate(2016, 8, 2).getTime()), Date.class));
		// Non assertable value, depends on the system timezone
		mapper.readValue("\"2016-08-02\"", Date.class);
	}

	@Test
	public void deserializationDateDecimal() throws IOException {
		Assertions.assertEquals(getDate(2016, 8, 2),
				mapper.readValue(String.valueOf(Double.valueOf((double) getDate(2016, 8, 2).getTime())), Date.class));
		// Non assertable value, depends on the system timezone
		mapper.readValue("\"2016-08-02\"", Date.class);
	}

	@Test
	public void deserializationLocalDate() throws IOException {
		Assertions.assertEquals("2016-08-02",
				mapper.readValue(String.valueOf(getDate(2016, 8, 2).getTime()), LocalDate.class).toString());
		Assertions.assertNull(mapper.readValue("\"2016-08-02\"", LocalDate.class));
	}

	@Test
	public void deserializationLocalDateTime() throws IOException {
		Assertions.assertEquals("2016-08-02T12:54:32", mapper
				.readValue(String.valueOf(getDate(2016, 8, 2, 12, 54, 32).getTime()), LocalDateTime.class).toString());
		Assertions.assertNull(mapper.readValue("\"2016-08-02\"", LocalDateTime.class));
	}

	@Test
	public void deserializationDateFailed() {
		Assertions.assertThrows(JsonParseException.class, () -> mapper.readValue("any", Date.class));
	}

	@Test
	public void deserializationEnum() throws IOException {
		Assertions.assertEquals(Status.ACCEPTED, mapper.readValue("\"accepted\"", Status.class));
		Assertions.assertEquals(Status.ACCEPTED, mapper.readValue("\"ACCEPTED\"", Status.class));
		Assertions.assertEquals(Status.ACCEPTED, vanillaMapper.readValue("\"ACCEPTED\"", Status.class));
		Assertions.assertThrows(InvalidFormatException.class,
				() -> vanillaMapper.readValue("\"accepted\"", Status.class));
	}

	@Test
	public void deserializationEnumFailed() {
		Assertions.assertThrows(InvalidFormatException.class, () -> mapper.readValue("\"some\"", Status.class));
	}

	@Test
	public void deserializationEnumFailed2() {
		Assertions.assertThrows(InvalidFormatException.class,
				() -> vanillaMapper.readValue("\"accepted\"", Status.class));
	}
}
