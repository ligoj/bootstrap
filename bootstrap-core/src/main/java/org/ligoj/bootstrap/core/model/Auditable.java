package org.ligoj.bootstrap.core.model;

import org.springframework.data.domain.Persistable;

/**
 * Interface for auditable entities. Allows storing and retrieving creation and modification information. The changing
 * instance (typically some user) is to be defined by a generics definition.
 * 
 * @param <U> the auditing type. Typically some kind of user.
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
}
