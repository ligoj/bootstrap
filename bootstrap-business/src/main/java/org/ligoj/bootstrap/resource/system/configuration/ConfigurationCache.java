package org.ligoj.bootstrap.resource.system.configuration;

import java.util.function.Function;

import org.ligoj.bootstrap.resource.system.cache.CacheProviderAware;
import org.springframework.stereotype.Component;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;

@Component
public class ConfigurationCache implements CacheProviderAware{

	@Override
	public void onCreate(HazelcastCacheManager cacheManager, final Function<String, CacheConfig<?, ?>> configProfider) {
		cacheManager.createCache("configuration", configProfider.apply("configuration"));
	}

}
