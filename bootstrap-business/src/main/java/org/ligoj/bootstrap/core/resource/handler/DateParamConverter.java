/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import java.util.Date;

import jakarta.ws.rs.ext.ParamConverter;

import org.ligoj.bootstrap.core.DateUtils;

/**
 * Build a {@link Date} from a UNIX timestamp value : the number of milliseconds since the Unix Epoch (1 January 1970
 * 00:00:00 UTC).
 */
public class DateParamConverter implements ParamConverter<Date> {

	@Override
	public Date fromString(final String value) {
		if (value == null) {
			return null;
		}
		return new Date(Long.parseLong(value));
	}

	@Override
	public String toString(final Date value) {
		return String.valueOf(value.toInstant().atZone(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}
}
