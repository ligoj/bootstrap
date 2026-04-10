/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import com.hazelcast.cache.HazelcastCacheManager;

/**
 * Callback when cache manager is built but not yet injected in the beans.
 */
public interface CacheManagerAware {

	/**
	 * Callback when cache manager is built but not yet injected in the beans.
	 *
	 * @param cacheManager The cache manger backed by {@link HazelcastCacheManager}.
	 * @param configurer   The {@link CacheConfigurer} initializer accepting the cache name and default TTL as parameters.
	 */
	void onCreate(HazelcastCacheManager cacheManager, CacheConfigurer configurer);

}
