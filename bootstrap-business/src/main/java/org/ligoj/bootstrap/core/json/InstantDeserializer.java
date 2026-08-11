/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import org.ligoj.bootstrap.core.DateUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.cfg.DateTimeFeature;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * {@link Instant} deserializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
public class InstantDeserializer extends tools.jackson.databind.ext.javatime.deser.InstantDeserializer<Instant> {

	private static final boolean DEFAULT_NORMALIZE_ZONE_ID = DateTimeFeature.NORMALIZE_DESERIALIZED_ZONE_ID.enabledByDefault();
	private static final boolean DEFAULT_ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS
			= DateTimeFeature.ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS.enabledByDefault();

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
	public Instant deserialize(final JsonParser parser, final DeserializationContext context) {
		var result = super.deserialize(parser, context);
		if (result != null) {
			result = result.atZone(DateUtils.getApplicationTimeZone().toZoneId()).toInstant();
		}
		return result;
	}

}
