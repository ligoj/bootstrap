/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import org.springframework.stereotype.Component;

import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;
import java.util.concurrent.TimeUnit;

/**
 * Configuration cache used for tests.
 */
@Component
class ConfigurationTestCache implements CacheManagerAware {

	@Override
	public void onCreate(HazelcastCacheManager cacheManager, final CacheConfigurer configurer) {
		final var config = configurer.newCacheConfig("test-cache");
		config.setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setSize(200));
		cacheManager.createCache("test-cache", config);

		final var tokens1 = configurer.newCacheConfig("test-cache-1", new Duration(TimeUnit.SECONDS, 1));
		tokens1.setEvictionConfig(new EvictionConfig());
		cacheManager.createCache("test-cache-1", tokens1);

		final var tokens2 = configurer.newCacheConfig("test-cache-2");
		tokens2.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 1)));
		tokens2.setEvictionConfig(new EvictionConfig());
		cacheManager.createCache("test-cache-2", tokens2);
	}

}
