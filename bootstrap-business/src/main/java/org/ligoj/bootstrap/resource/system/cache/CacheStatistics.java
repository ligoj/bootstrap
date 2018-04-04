/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
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
	private Long hitCount;
	private Long missCount;
	private Float hitPercentage;
	private Float missPercentage;
	private Float averageGetTime;

	/**
	 * The related cache node.
	 */
	private CacheNode node;
}
