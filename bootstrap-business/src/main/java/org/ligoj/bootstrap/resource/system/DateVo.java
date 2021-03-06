/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Date information.
 */
@Getter
@Setter
public class DateVo {


	/**
	 * Application timezone identifier.
	 */
	private String timeZone;

	/**
	 * Default timezone identifier.
	 */
	private String defaultTimeZone;

	/**
	 * Original default timezone identifier.
	 */
	private String originalDefaultTimeZone;

	/**
	 * System date regarding the application timezone.
	 */
	private Date date;

}
