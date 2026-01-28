/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import org.springframework.data.domain.Persistable;

import java.util.Map;

/**
 * Interface for auditable entities. Allows storing and retrieving creation and modification information. The changing
 * instance (typically some user) is to be defined by a generic definition.
 * 
 * @param <U> the auditing type. Typically, some kind of user.
 * @param <K> the type of the audited type's identifier.
 * @param <D> the type of the audit date.
 */
public interface Auditable<U, K, D> extends Persistable<K> {

	/**
	 * Returns the user who created this entity.
	 * 
	 * @return the createdBy
	 */
	U getCreatedBy();

	/**
	 * Sets the user who created this entity.
	 * 
	 * @param createdBy the creating entity to set
	 */
	void setCreatedBy(U createdBy);

	/**
	 * Returns the creation date of the entity.
	 * 
	 * @return the createdDate
	 */
	D getCreatedDate();

	/**
	 * Sets the creation date of the entity.
	 * 
	 * @param creationDate the creation date to set
	 */
	void setCreatedDate(D creationDate);

	/**
	 * Returns the user who modified the entity lastly.
	 * 
	 * @return the lastModifiedBy
	 */
	U getLastModifiedBy();

	/**
	 * Sets the user who modified the entity lastly.
	 * 
	 * @param lastModifiedBy the last modifying entity to set
	 */
	void setLastModifiedBy(U lastModifiedBy);

	/**
	 * Returns the date of the last modification.
	 * 
	 * @return the lastModifiedDate
	 */
	D getLastModifiedDate();

	/**
	 * Sets the date of the last modification.
	 * 
	 * @param lastModifiedDate the date of the last modification to set
	 */
	void setLastModifiedDate(D lastModifiedDate);

	/**
	 * Set the free form text at creation time only.
	 * @param context The free form text at creation time only.
	 */
	void setCreationContext(String context);

	/**
	 * Returns A free form text at creation time only.
	 *
	 * @return a free form text at creation time only.
	 */
	String getCreationContext();

	/**
	 * Set the free form text metadata.
	 * @param metadata The free form metadata.
	 */
	void setMetadata(Map<String, Object> metadata);

	/**
	 * Returns A free form metadata.
	 *
	 * @return a free form metadata.
	 */
	Map<String, Object> getMetadata();
}
