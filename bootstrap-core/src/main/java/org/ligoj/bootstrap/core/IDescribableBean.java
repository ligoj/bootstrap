package org.ligoj.bootstrap.core;

import java.io.Serializable;

/**
 * A common contract for entity and business object.
 * 
 * @param <K>
 *            The type of the identifier
 */
public interface IDescribableBean<K extends Serializable> extends INamableBean<K> {

	/**
	 * Bean description.
	 * 
	 * @return The description.
	 */
	String getDescription();

	/**
	 * Set the bean description.
	 * 
	 * @param description
	 *            The new description.
	 */
	void setDescription(String description);

}
