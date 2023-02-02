/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.test;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.ligoj.bootstrap.core.model.AbstractBusinessEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple business entity
 */
@Getter
@Setter
@Entity
@Table(name = "DEMO_DUMMY")
public class DummyBusinessEntity3 extends AbstractBusinessEntity<Integer> {

	@ManyToOne
	private DummyBusinessEntity3 parent;

}
