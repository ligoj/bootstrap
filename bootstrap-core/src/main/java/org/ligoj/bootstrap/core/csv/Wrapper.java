/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.csv;

import lombok.Getter;

/**
 * Supported CSV wrappers.
 */
@Getter
public enum Wrapper {

	/**
	 * Simple quote separator.
	 */
	QUOTE('\''),

	/**
	 * Double quote separator.
	 */
	DOUBLE_QUOTE('\"');

	/**
	 * Delimiter char.
	 */
	private final char delimiter;

	Wrapper(final char delimiter) {
		this.delimiter = delimiter;
	}

}
