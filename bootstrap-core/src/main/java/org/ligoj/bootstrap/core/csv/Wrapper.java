package org.ligoj.bootstrap.core.csv;

/**
 * Supported CSV wrappers.
 */
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

	/**
	 * Return the {@link #delimiter} value.
	 * 
	 * @return the {@link #delimiter} value.
	 */
	public char getDelimiter() {
		return delimiter;
	}

}
