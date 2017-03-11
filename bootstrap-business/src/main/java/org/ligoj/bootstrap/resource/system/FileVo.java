package org.ligoj.bootstrap.resource.system;

import lombok.Getter;
import lombok.Setter;

/**
 * File settings.
 */
@Getter
@Setter
public class FileVo {

	/**
	 * File system root
	 */
	private String absolutePath;

	/**
	 * Total space (bytes)
	 */
	private long totalSpace;

	/**
	 * Free space (bytes)
	 */
	private long freeSpace;

	/**
	 * Usable space (bytes)
	 */
	private long usableSpace;
}
