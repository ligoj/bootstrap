/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A cache cluster.
 */
@Getter
@Setter
public class CacheCluster {

	private String id;
	private String state;

	/**
	 * Cache cluster member. Not fully initialized to prevent recursive references.
	 */
	private List<CacheNode> members;
}
