/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import org.ligoj.bootstrap.core.DateUtils;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;

/**
 * {@link Instant} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class InstantSerializer extends StdSerializer<Instant> {

	/**
	 * JAX-RS serializer instance.
	 */
	public static final InstantSerializer INSTANCE = new InstantSerializer();

	protected InstantSerializer() {
		super(Instant.class);
	}

	@Override
	public void serialize(final Instant date, final JsonGenerator generator, final SerializationContext provider) {
		generator.writeNumber(date.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}
}
