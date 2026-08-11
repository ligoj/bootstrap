/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import org.ligoj.bootstrap.core.DateUtils;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDate;

/**
 * {@link LocalDate} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

	/**
	 * JAX-RS serializer instance.
	 */
	public static final LocalDateSerializer INSTANCE = new LocalDateSerializer();

	protected LocalDateSerializer() {
		super(LocalDate.class);
	}

	@Override
	public void serialize(final LocalDate date, final JsonGenerator generator, final SerializationContext provider) {
		generator.writeNumber(date.atStartOfDay(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}

}
