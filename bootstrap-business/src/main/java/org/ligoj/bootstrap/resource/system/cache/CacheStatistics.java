package org.ligoj.bootstrap.resource.system.cache;

import org.ligoj.bootstrap.core.NamedBean;
import lombok.Getter;
import lombok.Setter;

/**
 * Cache statistics
 */
@Getter
@Setter
public class CacheStatistics extends NamedBean<String> {

	private long size;
	private long hitCount;
	private long missCount;
	private long bytes;
	private long offHeapBytes;
}
