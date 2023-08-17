/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.dao.system;

import lombok.Getter;
import lombok.Setter;

/**
 * Bench result.
 */
@Getter
@Setter
public class BenchResult {

	/**
	 * Bench duration.
	 */
	private long duration;

	/**
	 * Managed entries.
	 */
	private int entries;

}
