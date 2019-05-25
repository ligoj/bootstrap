/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple entity with a variable type field.
 * 
 * @param <U> The tested many-to-one type.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractSuperVariableClass<U extends AbstractNamedEntity<Integer>>
		extends AbstractNamedEntity<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	private U child;
}
