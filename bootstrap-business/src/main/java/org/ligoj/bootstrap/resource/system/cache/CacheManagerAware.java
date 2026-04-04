/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;

import java.util.function.Function;

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
	default void onCreate(HazelcastCacheManager cacheManager, CacheConfigurer configurer) {
		onCreate(cacheManager, (name) -> configurer.newCacheConfig(name));
	}

	/**
	 * Callback when cache manager is built but not yet injected in the beans.
	 *
	 * @param cacheManager The cache manger backed by {@link HazelcastCacheManager}.
	 * @param provider
	 *            The {@link CacheConfig} initializer accepting the cache name as {@link Function} parameter.
	 */
	@Deprecated
	default void onCreate(final HazelcastCacheManager cacheManager, final Function<String, CacheConfig<?, ?>> provider) {
		// Ignore
	}

}
