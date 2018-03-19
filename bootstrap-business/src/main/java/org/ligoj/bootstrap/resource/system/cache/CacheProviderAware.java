package org.ligoj.bootstrap.resource.system.cache;

import java.util.function.Function;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;

@FunctionalInterface
public interface CacheProviderAware {

	void onCreate(HazelcastCacheManager cacheManager, Function<String, CacheConfig<?, ?>> configProfider);
}
