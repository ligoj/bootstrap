/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.Version;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * This mapper makes sure all enum values are converted in lower case. The conversion is done only once per value and cached internally by
 * the Enum[Serializer/Deserializer].
 */
public class ObjectMapperTrim extends JsonMapper {

	private static final long serialVersionUID = 1L;

	/**
	 * Serialize enum values in lower case.
	 */
	@SuppressWarnings("rawtypes")
	protected static class LowerCasingEnumSerializer extends StdSerializer<Enum> {

		public LowerCasingEnumSerializer() {
			super(Enum.class);
		}

		@Override
		public void serialize(Enum value, JsonGenerator generator, SerializationContext context) {
			generator.writeString(value.name().toLowerCase());
		}
	}

	/**
	 * Default constructor overriding the default annotation introspect.
	 */
	public ObjectMapperTrim() {
		super(createBuilder());
	}

	private static JsonMapper.Builder createBuilder() {
		final var module = new SimpleModule("BootstrapModule", new Version(1, 0, 1, null, null, null));

		// JSR 310 date management
		module.addDeserializer(Date.class, DateDeserializer.INSTANCE);
		module.addDeserializer(Instant.class, InstantDeserializer.INSTANCE);
		module.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
		module.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
		module.addSerializer(Date.class, DateSerializer.INSTANCE);
		module.addSerializer(Instant.class, InstantSerializer.INSTANCE);
		module.addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE);
		module.addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);

		// Case-insensitive enumeration
		module.addSerializer(Enum.class, new LowerCasingEnumSerializer());

		// Jackson 2.x defaults are kept for behavior compatibility (dates as timestamps, fail on
		// unknown properties). The mapper is immutable: 'serializationInclusion=NON_NULL',
		// previously injected by Spring, is now part of this builder.
		return JsonMapper.builderWithJackson2Defaults()
				.disable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
				// Jackson 3 reads/writes enums using 'toString()' by default, restore 'name()' semantic
				.disable(EnumFeature.READ_ENUMS_USING_TO_STRING, EnumFeature.WRITE_ENUMS_USING_TO_STRING)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				.changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_NULL))
				.addModule(module);
	}
}
