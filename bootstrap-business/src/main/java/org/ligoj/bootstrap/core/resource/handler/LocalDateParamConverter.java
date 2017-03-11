package org.ligoj.bootstrap.core.resource.handler;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import javax.ws.rs.ext.ParamConverter;

import org.ligoj.bootstrap.core.DateUtils;

/**
 * Build a {@link Date} from a UNIX timestamp value : the number of milliseconds since the Unix Epoch (1 January 1970
 * 00:00:00 UTC).
 */
public class LocalDateParamConverter implements ParamConverter<LocalDate> {

	@Override
	public LocalDate fromString(final String value) {
		if (value == null) {
			return null;
		}
		return Instant.ofEpochMilli(Long.parseLong(value)).atZone(DateUtils.getApplicationTimeZone().toZoneId()).toLocalDate();
	}

	@Override
	public String toString(final LocalDate value) {
		return String.valueOf(value.atStartOfDay(DateUtils.getApplicationTimeZone().toZoneId()).toInstant().toEpochMilli());
	}
}
