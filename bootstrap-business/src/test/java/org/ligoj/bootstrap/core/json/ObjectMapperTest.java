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
	public void testSerialization() throws JsonProcessingException {
		Assertions.assertEquals("\"accepted\"", mapper.writeValueAsString(Status.ACCEPTED));
		Assertions.assertEquals("\"ACCEPTED\"", vanillaMapper.writeValueAsString(Status.ACCEPTED));
	}

	@Test
	public void dateSerializer() throws IOException {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2).getTime()), mapper.writeValueAsString(getDate(2016, 8, 2)));
	}

	@Test
	public void localDateTimeSerializer() throws IOException {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2, 12, 54, 32).getTime()),
				mapper.writeValueAsString(LocalDateTime.of(2016, 8, 2, 12, 54, 32)));
	}

	@Test
	public void localDateSerializer() throws IOException {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2).getTime()), mapper.writeValueAsString(LocalDate.of(2016, 8, 2)));
	}

	@Test
	public void dateDeserializer() throws IOException {
		Assertions.assertEquals(getDate(2016, 8, 2), mapper.readValue(String.valueOf(getDate(2016, 8, 2).getTime()), Date.class));
		// Non assertable value, depends on the system timezone
		mapper.readValue("\"2016-08-02\"", Date.class);
	}

	@Test
	public void localDateDeserializer() throws IOException {
		Assertions.assertEquals("2016-08-02", mapper.readValue(String.valueOf(getDate(2016, 8, 2).getTime()), LocalDate.class).toString());
		Assertions.assertNull(mapper.readValue("\"2016-08-02\"", LocalDate.class));
	}

	@Test
	public void localDateTimeDeserializer() throws IOException {
		Assertions.assertEquals("2016-08-02T12:54:32",
				mapper.readValue(String.valueOf(getDate(2016, 8, 2, 12, 54, 32).getTime()), LocalDateTime.class).toString());
		Assertions.assertNull(mapper.readValue("\"2016-08-02\"", LocalDateTime.class));
	}

	@Test
	public void dateDeserializerFailed() throws IOException {
		Assertions.assertThrows(JsonParseException.class, () -> {
			Assertions.assertEquals(getDate(2016, 8, 2), mapper.readValue("any", Date.class));
		});
	}

	@Test
	public void localDateDeserializerFailed() throws IOException {
		Assertions.assertEquals("2016-08-02", mapper.readValue(String.valueOf(getDate(2016, 8, 2).getTime()), LocalDate.class).toString());
	}

	@Test
	public void localDateTimeDeserializerFailed() throws IOException {
		Assertions.assertEquals("2016-08-02T12:54:32",
				mapper.readValue(String.valueOf(getDate(2016, 8, 2, 12, 54, 32).getTime()), LocalDateTime.class).toString());
	}

	@Test
	public void testDeserialization() throws IOException {
		Assertions.assertEquals(Status.ACCEPTED, mapper.readValue("\"accepted\"", Status.class));
		Assertions.assertEquals(Status.ACCEPTED, vanillaMapper.readValue("\"ACCEPTED\"", Status.class));
	}

	@Test
	public void testDeserializationFailed() throws IOException {
		Assertions.assertThrows(InvalidFormatException.class, () -> {
			Assertions.assertEquals(Status.ACCEPTED, mapper.readValue("\"ACCEPTED\"", Status.class));
		});
	}

	@Test
	public void testDeserializationFailed2() throws IOException {
		Assertions.assertThrows(InvalidFormatException.class, () -> {
			Assertions.assertEquals(Status.ACCEPTED, vanillaMapper.readValue("\"accepted\"", Status.class));
		});
	}
}
