/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.test;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
