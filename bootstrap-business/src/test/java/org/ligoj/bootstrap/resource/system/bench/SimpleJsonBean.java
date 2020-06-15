/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.bench;

/**
 * Simple bean for JSon tests.
 */
class SimpleJsonBean {

	/**
	 * Key.
	 */
	private String key;

	/**
	 * Value
	 */
	private Integer value;

	/**
	 * Return the {@link #key} value.
	 * 
	 * @return the {@link #key} value.
	 */
	String getKey() {
		return key;
	}

	/**
	 * Set the {@link #key} value.
	 * 
	 * @param key
	 *            the {@link #key} to set.
	 */
	void setKey(final String key) {
		this.key = key;
	}

	/**
	 * Return the {@link #value} value.
	 * 
	 * @return the {@link #value} value.
	 */
	Integer getValue() {
		return value;
	}

	/**
	 * Set the {@link #value} value.
	 * 
	 * @param value
	 *            the {@link #value} to set.
	 */
	void setValue(final Integer value) {
		this.value = value;
	}

}
