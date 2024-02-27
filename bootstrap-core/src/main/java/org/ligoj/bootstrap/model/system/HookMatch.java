/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.model.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Hook descriptor.
 */
@JsonIgnoreProperties
@Getter
@Setter
public class HookMatch implements Serializable {
	/**
	 * Watched path expression.
	 */
	private String path;

	/**
	 * Watched method. Can be <code>null</code> for all methods. Upper case.
	 */
	private String method;
}