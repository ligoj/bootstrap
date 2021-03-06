/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system;

import lombok.Getter;
import lombok.Setter;

/**
 * CPU information.
 */
@Getter
@Setter
public class CpuVo {

	/**
	 * Total number of processors or cores available to the JVM
	 */
	private int total;

}
