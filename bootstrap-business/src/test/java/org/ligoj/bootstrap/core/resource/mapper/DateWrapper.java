/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.mapper;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

/**
 * Date attributes wrapper to check serialization and de-serializations inside a JaxRS context.
 */
@Getter
@Setter
public class DateWrapper {

	private Instant instant;

	private LocalDate localDate;

	private Date date;

}
