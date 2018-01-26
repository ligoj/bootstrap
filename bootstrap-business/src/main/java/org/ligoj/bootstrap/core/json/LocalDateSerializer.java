package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.time.LocalDate;

import org.ligoj.bootstrap.core.DateUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * {@link LocalDate} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	public static final LocalDateSerializer INSTANCE = new LocalDateSerializer();

	protected LocalDateSerializer() {
		super(LocalDate.class, false);
	}

	@Override
	public void serialize(final LocalDate date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
		generator.writeNumber(date.atStartOfDay(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}

}
