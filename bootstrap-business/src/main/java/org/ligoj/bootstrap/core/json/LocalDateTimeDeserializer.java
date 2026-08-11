/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import org.ligoj.bootstrap.core.DateUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * {@link Date} deserializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

	/**
	 * JAX-RS serializer instance.
	 */
	public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer();

	protected LocalDateTimeDeserializer() {
		super(LocalTime.class);
	}

	@Override
	public LocalDateTime deserialize(final JsonParser parser, final DeserializationContext context) {
        if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return Instant.ofEpochMilli(parser.getLongValue()).atZone(DateUtils.getApplicationTimeZone().toZoneId()).toLocalDateTime();
        }
		return null;
	}

}
