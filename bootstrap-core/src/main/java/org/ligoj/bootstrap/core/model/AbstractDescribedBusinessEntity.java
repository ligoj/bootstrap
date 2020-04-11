/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.hibernate.validator.constraints.Length;
import org.ligoj.bootstrap.core.IDescribableBean;
import org.ligoj.bootstrap.core.validation.SafeHtml;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract entity with business key, name and description.
 * 
 * @param <K>
 *            the type of the identifier
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractDescribedBusinessEntity<K extends Serializable> extends AbstractNamedBusinessEntity<K> implements
		IDescribableBean<K> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Human readable description. Accepting safe HTML content.
	 */
	@Length(max = 250)
	@SafeHtml
	private String description;

}
