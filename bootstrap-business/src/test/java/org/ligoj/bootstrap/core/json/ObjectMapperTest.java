/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.AbstractDataGeneratorTest;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Test class of {@link ObjectMapperTrim}
 */
class ObjectMapperTest extends AbstractDataGeneratorTest {

	private final ObjectMapper mapper = new ObjectMapperTrim();
	private final ObjectMapper vanillaMapper = JsonMapper.builderWithJackson2Defaults()
			.disable(EnumFeature.READ_ENUMS_USING_TO_STRING, EnumFeature.WRITE_ENUMS_USING_TO_STRING).build();

	@Test
	void serializationEnum() throws JacksonException {
		Assertions.assertEquals("\"accepted\"", mapper.writeValueAsString(Status.ACCEPTED));
		Assertions.assertEquals("\"ACCEPTED\"", vanillaMapper.writeValueAsString(Status.ACCEPTED));
	}

	@Test
	void serializationDate() {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2).getTime()),
				mapper.writeValueAsString(getDate(2016, 8, 2)));
	}

	@Test
	void serializationInstant() {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2).getTime()),
				mapper.writeValueAsString(getDate(2016, 8, 2).toInstant()));
	}

	@Test
	void serializationLocalDateTime() {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2, 12, 54, 32).getTime()),
				mapper.writeValueAsString(LocalDateTime.of(2016, 8, 2, 12, 54, 32)));
	}

	@Test
	void serializationLocalDate() {
		Assertions.assertEquals(String.valueOf(getDate(2016, 8, 2).getTime()),
				mapper.writeValueAsString(LocalDate.of(2016, 8, 2)));
	}

	@Test
	void deserializationDate() {
		Assertions.assertEquals(getDate(2016, 8, 2),
				mapper.readValue(String.valueOf(getDate(2016, 8, 2).getTime()), Date.class));
		// Non-assertable value, depends on the system timezone
		mapper.readValue("\"2016-08-02\"", Date.class);
	}

	@Test
	void deserializationDateDecimal() {
		Assertions.assertEquals(getDate(2016, 8, 2),
				mapper.readValue(String.valueOf(Double.valueOf(getDate(2016, 8, 2).getTime())), Date.class));
		// Non-assertable value, depends on the system timezone
		mapper.readValue("\"2016-08-02\"", Date.class);
	}

	@Test
	void deserializationLocalDate() {
		Assertions.assertEquals("2016-08-02",
				mapper.readValue(String.valueOf(getDate(2016, 8, 2).getTime()), LocalDate.class).toString());
		Assertions.assertNull(mapper.readValue("\"2016-08-02\"", LocalDate.class));
	}

	@Test
	void deserializationLocalDateTime() {
		Assertions.assertEquals("2016-08-02T12:54:32", mapper
				.readValue(String.valueOf(getDate(2016, 8, 2, 12, 54, 32).getTime()), LocalDateTime.class).toString());
		Assertions.assertNull(mapper.readValue("\"2016-08-02\"", LocalDateTime.class));
	}

	@Test
	void deserializationDateFailed() {
		Assertions.assertThrows(StreamReadException.class, () -> mapper.readValue("any", Date.class));
	}

	@Test
	void deserializationEnum() {
		Assertions.assertEquals(Status.ACCEPTED, mapper.readValue("\"accepted\"", Status.class));
		Assertions.assertEquals(Status.ACCEPTED, mapper.readValue("\"ACCEPTED\"", Status.class));
		Assertions.assertEquals(Status.ACCEPTED, vanillaMapper.readValue("\"ACCEPTED\"", Status.class));
		Assertions.assertThrows(InvalidFormatException.class,
				() -> vanillaMapper.readValue("\"accepted\"", Status.class));
	}

	@Test
	void deserializationEnumFailed() {
		Assertions.assertThrows(InvalidFormatException.class, () -> mapper.readValue("\"some\"", Status.class));
	}

	@Test
	void deserializationEnumFailed2() {
		Assertions.assertThrows(InvalidFormatException.class,
				() -> vanillaMapper.readValue("\"accepted\"", Status.class));
	}

	@Test
	void deserializationInstant() throws ParseException {
		// Explicit locale and time zone: SHORT pattern is locale-dependent (US 'M/d/yy' vs FR 'dd/MM/y')
		// and lenient parsing resolves the day at midnight of the format's time zone
		final var format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		final var instant = format.parse("2025/11/09 01:02:03").toInstant();
		Assertions.assertEquals("6554217600000", mapper.writeValueAsString(instant));
		Assertions.assertEquals(instant, mapper.readValue("6554217600000", Instant.class));
	}

}
