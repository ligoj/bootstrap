package org.ligoj.bootstrap.core.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.hibernate.validator.constraints.Length;

import org.ligoj.bootstrap.core.IDescribableBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract entity with business key, name and description.
 * 
 * @param <ID>
 *            the type of the identifier
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractDescribedBusinessEntity<ID extends Serializable> extends AbstractNamedBusinessEntity<ID> implements
		IDescribableBean<ID> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Human readable description.
	 */
	@Length(max = 250)
	private String description;

}
