/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.io.IOException;

import org.ligoj.bootstrap.core.INamableBean;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serialize entities with their name.
 */
public class ToNameSerializer extends StdSerializer<INamableBean<?>> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * JAX-RS serializer instance.
	 */
	public static final ToNameSerializer INSTANCE = new ToNameSerializer();

	/**
	 * Default constructor.
	 */
	protected ToNameSerializer() {
		super(INamableBean.class);
	}

	@Override
	public void serialize(final INamableBean<?> date, final JsonGenerator generator, final SerializationContext provider) {
		generator.writeString(date.getName());
	}

}
