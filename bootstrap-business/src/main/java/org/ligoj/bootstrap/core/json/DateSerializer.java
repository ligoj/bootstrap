/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import lombok.Setter;
import org.ligoj.bootstrap.core.DateUtils;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Date;

/**
 * {@link Date} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
@Setter
public class DateSerializer extends StdSerializer<Date> {

	/**
	 * JAX-RS serializer instance.
	 */
	public static final DateSerializer INSTANCE = new DateSerializer();

	protected DateSerializer() {
		super(Date.class);
	}

	@Override
	public void serialize(final Date date, final JsonGenerator generator, final SerializationContext provider) {
		generator.writeNumber(date.getTime());
	}

}
