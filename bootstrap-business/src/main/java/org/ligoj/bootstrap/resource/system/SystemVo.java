/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

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
