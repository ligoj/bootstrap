/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple business entity with defined variable type.
 */
@Getter
@Setter
@Entity
public class DummyChildClass extends AbstractSuperVariableClass<DummyNamedEntity> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

}
