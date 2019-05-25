/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
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
public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

	
	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * JAX-RS serializer instance.
	 */
	public static final LocalDateDeserializer INSTANCE = new LocalDateDeserializer();

	protected LocalDateDeserializer() {
		super(LocalTime.class);
	}

	@Override
	public LocalDate deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			return Instant.ofEpochMilli(parser.getLongValue()).atZone(DateUtils.getApplicationTimeZone().toZoneId()).toLocalDate();
		}
		return null;
	}

}
