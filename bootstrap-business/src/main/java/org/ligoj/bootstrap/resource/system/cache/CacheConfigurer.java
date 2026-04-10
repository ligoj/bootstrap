/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import com.hazelcast.config.CacheConfig;

import javax.cache.expiry.Duration;

/**
 * Helper of cache configuration. See {@link MergedHazelCastManagerFactoryBean} and {@link CacheManagerAware}.
 */
public interface CacheConfigurer {

	/**
	 * Create a new {@link CacheConfig} with configured settings before {@link CacheManagerAware} implementor.
	 *
	 * @param name            The cache name to configure.
	 * @param defaultDuration The default TTL in seconds.
	 * @return The created  {@link CacheConfig} with the policy.
	 */
	CacheConfig<String, Object> newCacheConfig(final String name, final Duration defaultDuration);

	/**
	 * Create a new {@link CacheConfig} with configured settings before {@link CacheManagerAware} implementor.
	 *
	 * @param name            The cache name to configure.
	 * @return The created  {@link CacheConfig} with the policy.
	 */
	default CacheConfig<String, Object> newCacheConfig(final String name) {
		return newCacheConfig(name, Duration.ETERNAL);
	}
}
