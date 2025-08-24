/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.ligoj.bootstrap.core.DateUtils;

import java.io.IOException;
import java.time.Instant;

/**
 * {@link Instant} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class InstantSerializer extends StdSerializer<Instant> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * JAX-RS serializer instance.
	 */
	public static final InstantSerializer INSTANCE = new InstantSerializer();

	protected InstantSerializer() {
		super(Instant.class, false);
	}

	@Override
	public void serialize(final Instant date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
		generator.writeNumber(date.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}
}
