package org.ligoj.bootstrap.core;

import java.io.Serializable;

/**
 * A common contract for entity and business object.
 * 
 * @param <ID>
 *            the type of the identifier
 */
public interface IDescribableBean<ID extends Serializable> extends INamableBean<ID> {

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
