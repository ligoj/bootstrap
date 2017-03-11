package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.ligoj.bootstrap.core.DateUtils;
import lombok.Setter;

/**
 * {@link Date} serializer using the shared {@link DateUtils#getApplicationTimeZone()}.
 */
@Setter
public class DateSerializer extends StdSerializer<Date> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	public static final DateSerializer INSTANCE = new DateSerializer();

	protected DateSerializer() {
		super(Date.class, false);
	}

	@Override
	public void serialize(final Date date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
		generator.writeNumber(date.getTime());
	}

}
