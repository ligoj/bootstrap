/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeFeature;
import org.ligoj.bootstrap.core.DateUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * {@link Instant} deserializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class InstantDeserializer extends com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer<Instant> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	private final static boolean DEFAULT_NORMALIZE_ZONE_ID = JavaTimeFeature.NORMALIZE_DESERIALIZED_ZONE_ID.enabledByDefault();
	private final static boolean DEFAULT_ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS
			= JavaTimeFeature.ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS.enabledByDefault();

	/**
	 * JAX-RS serializer instance.
	 */
	public static final InstantDeserializer INSTANCE = new InstantDeserializer();

	protected InstantDeserializer() {
		super(
		Instant.class, DateTimeFormatter.ISO_INSTANT,
				Instant::from,
				a -> Instant.ofEpochMilli(a.value),
				a -> Instant.ofEpochSecond(a.integer, a.fraction),
				null,
				true, // yes, replace zero offset with Z
				DEFAULT_NORMALIZE_ZONE_ID,
				DEFAULT_ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS
		);
	}

	@Override
	public Instant deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		var result = super.deserialize(parser, context);
		if (result != null) {
			result = result.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toInstant();
		}
		return result;
	}

}
