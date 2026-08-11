/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.springframework.data.domain.Persistable;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serialize entities with their identifier.
 */
public class ToIdSerializer extends StdSerializer<Persistable<?>> {

	/**
	 * JAX-RS serializer instance.
	 */
	public static final ToIdSerializer INSTANCE = new ToIdSerializer();

	/**
	 * Default constructor.
	 */
	protected ToIdSerializer() {
		super(Persistable.class);
	}

	@Override
	public void serialize(final Persistable<?> bean, final JsonGenerator generator, final SerializationContext provider) {
		if (bean.getId() instanceof Number n) {
			// Numeric, but no decimal accepted
			generator.writeNumber(n.longValue());
		} else {
			// Consider ID as a String (not failsafe)
			generator.writeString((String) bean.getId());
		}
	}

}
