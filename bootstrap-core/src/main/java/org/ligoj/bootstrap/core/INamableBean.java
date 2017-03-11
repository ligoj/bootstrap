package org.ligoj.bootstrap.core;

import java.io.Serializable;

/**
 * A named and identified bean
 * 
 * @param <ID>
 *            the type of the identifier
 */
public interface INamableBean<ID extends Serializable> extends Comparable<INamableBean<ID>> {

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
	ID getId();

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
	void setId(ID id);

	@Override
	default int compareTo(final INamableBean<ID> o) {
		return getName().compareToIgnoreCase(o.getName());
	}
}
