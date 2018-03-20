package org.ligoj.bootstrap.core;

import java.io.Serializable;

/**
 * A named and identified bean
 * 
 * @param <K>
 *            The type of the identifier
 */
public interface INamableBean<K extends Serializable> extends Comparable<INamableBean<K>>, Serializable {

	/**
	 * Bean name.
	 * 
	 * @return human readable name.
	 */
	String getName();

	/**
	 * Bean identifier.
	 * 
	 * @return identifier.
	 */
	K getId();

	/**
	 * Set the bean name.
	 * 
	 * @param name
	 *            The new name.
	 */
	void setName(String name);

	/**
	 * Set the bean identifier.
	 * 
	 * @param id
	 *            The new identifier.
	 */
	void setId(K id);

	@Override
	default int compareTo(final INamableBean<K> o) {
		return getName().compareToIgnoreCase(o.getName());
	}
}
