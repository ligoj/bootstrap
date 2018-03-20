package org.ligoj.bootstrap.resource.system.cache;

import lombok.Getter;
import lombok.Setter;

/**
 * Cache statistics
 */
@Getter
@Setter
public class CacheStatistics {

	private String id;
	private long size;
	private long hitCount;
	private long missCount;
	private float missPercentage;
	private float averageGetTime;

	/**
	 * The related cache node.
	 */
	private CacheNode node;
}
