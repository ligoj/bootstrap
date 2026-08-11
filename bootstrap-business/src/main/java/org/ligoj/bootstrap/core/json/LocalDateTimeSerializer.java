/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.time.LocalDateTime;

import org.ligoj.bootstrap.core.DateUtils;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * {@link LocalDateTime} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * JAX-RS serializer instance.
	 */
	public static final LocalDateTimeSerializer INSTANCE = new LocalDateTimeSerializer();

	protected LocalDateTimeSerializer() {
		super(LocalDateTime.class);
	}

	@Override
	public void serialize(final LocalDateTime date, final JsonGenerator generator, final SerializationContext provider) {
		generator.writeNumber(date.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}

}
