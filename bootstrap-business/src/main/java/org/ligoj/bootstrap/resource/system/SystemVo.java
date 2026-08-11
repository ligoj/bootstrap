/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * System information.
 */
@Getter
@Setter
public class SystemVo {

	private MemoryVo memory;
	private CpuVo cpu;
	private DateVo date;
	private List<FileVo> files;

}
