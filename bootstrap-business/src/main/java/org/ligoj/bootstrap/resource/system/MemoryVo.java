/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system;

import lombok.Getter;
import lombok.Setter;

/**
 * Memory settings.
 */
@Getter
@Setter
public class MemoryVo {

	/**
	 * Total amount of free memory available to the JVM.
	 */
	private long freeMemory;

	/**
	 * Maximum amount of memory the JVM will attempt to use.
	 */
	private long maxMemory;

	/**
	 * Total memory currently in use by the JVM
	 */
	private long totalMemory;
}
