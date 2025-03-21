/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Simple extension to expose the identifier setter.
 * 
 * @param <K>
 *            The primary key's type
 */
@JsonIgnoreProperties("new")
public abstract class AbstractPersistable<K extends Serializable> extends org.springframework.data.jpa.domain.AbstractPersistable<K> {

	@Override
	public void setId(final K id) { // NOPMD NOSONAR -- Need to extend the visibility
		super.setId(id);
	}
}
