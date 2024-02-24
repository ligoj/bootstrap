/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Javadoc's method structure.
 */
@Getter
@AllArgsConstructor
class MethodDocs {

	/**
	 * Method definition
	 */
	private final String methodInfo;

	/**
	 * Method's parameters definition
	 */
	private final List<String> paramInfo;

	/**
	 * Method's return definition
	 */
	private final String returnInfo;

}