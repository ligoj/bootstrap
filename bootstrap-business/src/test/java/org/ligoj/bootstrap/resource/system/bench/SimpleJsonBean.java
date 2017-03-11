package org.ligoj.bootstrap.resource.system.bench;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple bean for JSon tests.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class SimpleJsonBean {

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
	public String getKey() {
		return key;
	}

	/**
	 * Set the {@link #key} value.
	 * 
	 * @param key
	 *            the {@link #key} to set.
	 */
	public void setKey(final String key) {
		this.key = key;
	}

	/**
	 * Return the {@link #value} value.
	 * 
	 * @return the {@link #value} value.
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * Set the {@link #value} value.
	 * 
	 * @param value
	 *            the {@link #value} to set.
	 */
	public void setValue(final Integer value) {
		this.value = value;
	}

}
