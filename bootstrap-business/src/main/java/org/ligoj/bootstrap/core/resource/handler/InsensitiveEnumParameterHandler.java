/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Optional;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Insensitive {@link Enum} converter.
 */
@Provider
public class InsensitiveEnumParameterHandler implements ParamConverterProvider {

	@SuppressWarnings("unchecked")
	@Override
	public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType, final Annotation[] annotations) { // NOSONAR
		if (rawType.isEnum()) {
			@SuppressWarnings("rawtypes")
			final Class<Enum> enumType = (Class<Enum>) rawType;
			return new ParamConverter<>() {

				@Override
				public T fromString(final String value) {
					return (T) Optional.ofNullable(EnumUtils.getEnum(enumType, value))
							.orElseGet(() -> Optional.ofNullable(EnumUtils.getEnum(enumType, StringUtils.upperCase(value, Locale.ENGLISH)))
									.orElseGet(() -> EnumUtils.getEnum(enumType, StringUtils.lowerCase(value, Locale.ENGLISH))));
				}

				@Override
				public String toString(final T value) {
					return ((Enum<?>) value).name().toLowerCase(Locale.ENGLISH);
				}
			};
		}
		return null;
	}
}