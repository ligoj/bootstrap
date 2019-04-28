/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Date;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

/**
 * Build a {@link Date} from a UNIX timestamp value : the number of milliseconds since the Unix Epoch (1 January 1970
 * 00:00:00 UTC).
 */
@Provider
public class UnixTimestampParameterHandler implements ParamConverterProvider {

	private final DateParamConverter converter = new DateParamConverter();
	private final LocalDateParamConverter localDateConverter = new LocalDateParamConverter();

	@SuppressWarnings("unchecked")
	@Override
	public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType, final Annotation[] annotations) { // NOSONAR
		
		// Basic date handler
		if (rawType.equals(Date.class)) {
			return (ParamConverter<T>) converter;
		}
		
		// LocalDate handler
		if (rawType.equals(LocalDate.class)) {
			return (ParamConverter<T>) localDateConverter;
		}
		return null;
	}
}