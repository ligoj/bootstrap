package org.ligoj.bootstrap.resource.system.cache;

import java.util.function.Function;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;

/**
 * Callback when cache manager is built but not yet injected in the beans.
 */
@FunctionalInterface
public interface CacheManagerAware {

	/**
	 * Callback when cache manager is built but not yet injected in the beans.
	 * 
	 * @param cacheManager
	 *            The cache manger backed by {@link HazelcastCacheManager}.
	 * @param provider
	 *            The {@link CacheConfig} initializer accepting the cache name as {@link Function} parameter.
	 */
	void onCreate(HazelcastCacheManager cacheManager, Function<String, CacheConfig<?, ?>> provider);
}
