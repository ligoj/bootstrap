package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.time.LocalDateTime;

import org.ligoj.bootstrap.core.DateUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * {@link LocalDateTime} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	public static final LocalDateTimeSerializer INSTANCE = new LocalDateTimeSerializer();

	protected LocalDateTimeSerializer() {
		super(LocalDateTime.class, false);
	}

	@Override
	public void serialize(final LocalDateTime date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
		generator.writeNumber(date.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}

}
