package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.ligoj.bootstrap.core.DateUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * {@link Date} deserializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer();

	protected LocalDateTimeDeserializer() {
		super(LocalTime.class);
	}

	@Override
	public LocalDateTime deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
            return Instant.ofEpochMilli(parser.getLongValue()).atZone(DateUtils.getApplicationTimeZone().toZoneId()).toLocalDateTime();
        }
		return null;
	}

}
