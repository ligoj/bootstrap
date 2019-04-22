/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.ligoj.bootstrap.core.DateUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * {@link Date} deserializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class DateDeserializer extends StdDeserializer<Date> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	public static final DateDeserializer INSTANCE = new DateDeserializer();

	protected DateDeserializer() {
		super(Date.class);
	}

	@Override
	public Date deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		// Timestamp epoch milliseconds long support
		if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			final Calendar newCalendar = DateUtils.newCalendar();
			newCalendar.setTimeInMillis(parser.getLongValue());
			return newCalendar.getTime();
		}
		
		// Timestamp epoch milliseconds "double" type support
		if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
			final Calendar newCalendar = DateUtils.newCalendar();
			newCalendar.setTimeInMillis((long)parser.getDoubleValue());
			return newCalendar.getTime();
		}
		return _parseDate(parser, context);
	}

}
