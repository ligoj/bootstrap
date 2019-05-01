/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple named entity
 */
@Getter
@Setter
@Entity
public class DummyNamedEntity extends AbstractNamedEntity<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;
}
