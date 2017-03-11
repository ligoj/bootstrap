package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Simple extension to expose the identifier setter.
 * 
 * @param <ID>
 *            The primary key's type
 */
@JsonIgnoreProperties(value = "new")
public abstract class AbstractPersistable<ID extends Serializable> extends org.springframework.data.jpa.domain.AbstractPersistable<ID> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setId(final ID id) { // NOPMD NOSONAR -- Need to extend the visibility
		super.setId(id);
	}
}
