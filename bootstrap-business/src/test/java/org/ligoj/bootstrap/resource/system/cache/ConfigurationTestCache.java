package org.ligoj.bootstrap.resource.system.cache;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;

@Component
public class ConfigurationTestCache implements CacheManagerAware {

	@Override
	public void onCreate(HazelcastCacheManager cacheManager, final Function<String, CacheConfig<?, ?>> configProfider) {
		final CacheConfig<?, ?> config = configProfider.apply("test-cache");
		config.setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU)
				.setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.ENTRY_COUNT).setSize(200));
		cacheManager.createCache("test-cache", config);
	}

}
