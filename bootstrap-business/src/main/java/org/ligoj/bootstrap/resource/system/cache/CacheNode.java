package org.ligoj.bootstrap.resource.system.cache;

import lombok.Getter;
import lombok.Setter;

/**
 * A cache node.
 */
@Getter
@Setter
public class CacheNode {

	/**
	 * Node identifier.
	 */
	private String id;

	/**
	 * Includes IP and port.
	 */
	private String address;

	/**
	 * Version of this node.
	 */
	private String version;
	
	private CacheCluster cluster;
}
