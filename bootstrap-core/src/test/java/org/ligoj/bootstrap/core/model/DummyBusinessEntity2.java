/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple business entity
 */
@Getter
@Setter
@SuppressWarnings("all")
public class DummyBusinessEntity2 extends AbstractBusinessEntity<String> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	@java.lang.SuppressWarnings("all")
	@jakarta.annotation.Generated("lombok")
	protected boolean canEqual(final java.lang.Object other) {
		return other instanceof DummyBusinessEntity2;
	}
}
