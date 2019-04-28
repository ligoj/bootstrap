/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import javax.cache.expiry.TouchedExpiryPolicy;

import org.springframework.stereotype.Component;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;

@Component
public class ConfigurationTestCache implements CacheManagerAware {

	@Override
	public void onCreate(HazelcastCacheManager cacheManager, final Function<String, CacheConfig<?, ?>> provider) {
		final var config = provider.apply("test-cache");
		config.setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU)
				.setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.ENTRY_COUNT).setSize(200));
		cacheManager.createCache("test-cache", config);

	
		final var tokens1 = provider.apply("test-cache-1");
		tokens1.setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 1)));
		tokens1.setEvictionConfig(new EvictionConfig() );
		cacheManager.createCache("test-cache-1", tokens1);

		final var tokens2 = provider.apply("test-cache-2");
		tokens2.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 1)));
		tokens2.setEvictionConfig(new EvictionConfig() );
		cacheManager.createCache("test-cache-2", tokens2);
}

}
