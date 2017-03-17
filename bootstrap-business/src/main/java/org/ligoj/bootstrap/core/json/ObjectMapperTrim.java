package org.ligoj.bootstrap.core.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * This mapper makes sure all enum values are converted in lower case. The conversion is done only once per value and
 * cached internally by the Enum[Serializer/Deserializer].
 * 
 * @author Fabrice Daugan
 */
public class ObjectMapperTrim extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	/**
	 * Extend the {@link JacksonAnnotationIntrospector} only for enum value.
	 */
	protected static final AnnotationIntrospector CUSTOM_ANNOTATION_INTROSPECTOR = new JacksonAnnotationIntrospector() {

		private static final long serialVersionUID = 1L;

		/**
		 * @deprecated And yet no other global configuration is possible
		 */
		@Override
		@Deprecated
		public String findEnumValue(final Enum<?> value) {
			// Simple override, just before the save in the cache. Is used for both serialization processes.
			return value.name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public String[] findEnumValues(final Class<?> enumType, final Enum<?>[] enumValues, final String[] names) {
			for (int i = 0; i < enumValues.length; ++i) {
				names[i] = findEnumValue(enumValues[i]);
			}
			return names;
		}

	};

	/**
	 * Default constructor overriding the default annotation introspector.
	 */
	public ObjectMapperTrim() {
		setAnnotationIntrospector(CUSTOM_ANNOTATION_INTROSPECTOR);
		final SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null, null, null));
		testModule.addDeserializer(Date.class, DateDeserializer.INSTANCE);
		testModule.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
		testModule.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
		testModule.addSerializer(Date.class, DateSerializer.INSTANCE);
		testModule.addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE);
		testModule.addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);
		registerModule(testModule);
	}
}
