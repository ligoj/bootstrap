/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import org.ligoj.bootstrap.core.DateUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Date;

/**
 * {@link Date} deserializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class DateDeserializer extends StdDeserializer<Date> {

	/**
	 * JAX-RS serializer instance.
	 */
	public static final DateDeserializer INSTANCE = new DateDeserializer();

	protected DateDeserializer() {
		super(Date.class);
	}

	@Override
	public Date deserialize(final JsonParser parser, final DeserializationContext context) {
		// Timestamp epoch milliseconds long support
		if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
			final var newCalendar = DateUtils.newCalendar();
			newCalendar.setTimeInMillis(parser.getLongValue());
			return newCalendar.getTime();
		}

		// Timestamp epoch milliseconds "double" type support
		if (parser.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
			final var newCalendar = DateUtils.newCalendar();
			newCalendar.setTimeInMillis((long) parser.getDoubleValue());
			return newCalendar.getTime();
		}
		return _parseDate(parser, context);
	}

}
