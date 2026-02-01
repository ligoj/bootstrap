/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.api;

import lombok.Getter;
import lombok.Setter;
import org.ligoj.bootstrap.core.NamedBean;

import java.time.Instant;

/**
 * API token.
 */
@Getter
@Setter
public class SystemApiTokenEdition extends NamedBean<Integer> {

	/**
	 * SID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Optional maximal usage date of this token.
	 */
	private Instant expiration;
}
