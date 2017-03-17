package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Simple extension to expose the identifier setter.
 * 
 * @param <K>
 *            The primary key's type
 */
@JsonIgnoreProperties(value = "new")
public abstract class AbstractPersistable<K extends Serializable> extends org.springframework.data.jpa.domain.AbstractPersistable<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void setId(final K id) { // NOPMD NOSONAR -- Need to extend the visibility
		super.setId(id);
	}
}
