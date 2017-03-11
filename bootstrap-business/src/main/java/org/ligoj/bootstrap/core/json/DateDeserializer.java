package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.ligoj.bootstrap.core.DateUtils;

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
		if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
			final Calendar newCalendar = DateUtils.newCalendar();
			newCalendar.setTimeInMillis(parser.getLongValue());
			return newCalendar.getTime();
		}
		return _parseDate(parser, context);
	}

}
