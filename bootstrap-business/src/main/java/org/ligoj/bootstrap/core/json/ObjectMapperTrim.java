/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * This mapper makes sure all enum values are converted in lower case. The conversion is done only once per value and
 * cached internally by the Enum[Serializer/Deserializer].
 */
public class ObjectMapperTrim extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	/**
	 * Extend the {@link JacksonAnnotationIntrospector} only for enum value.
	 */
	@SuppressWarnings({ "serial", "rawtypes" })
	protected static class LowerCasingEnumSerializer extends StdSerializer<Enum> {

		public LowerCasingEnumSerializer() {
			super(Enum.class);
		}

		@Override
		public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeString(value.name().toLowerCase());
		}
	};

	/**
	 * Default constructor overriding the default annotation introspector.
	 */
	public ObjectMapperTrim() {
		final SimpleModule module = new SimpleModule("BootstrapModule", new Version(1, 0, 1, null, null, null));
		module.addDeserializer(Date.class, DateDeserializer.INSTANCE);
		module.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
		module.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
		module.addSerializer(Date.class, DateSerializer.INSTANCE);
		module.addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE);
		module.addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);
		module.addSerializer(Enum.class, new LowerCasingEnumSerializer());
		
		// Case insensitive enumeration
		enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
		registerModule(module);
	}
}
