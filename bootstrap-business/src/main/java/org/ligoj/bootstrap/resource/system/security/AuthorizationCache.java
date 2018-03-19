package org.ligoj.bootstrap.resource.system.security;

import java.util.function.Function;

import org.ligoj.bootstrap.resource.system.cache.CacheProviderAware;
import org.springframework.stereotype.Component;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;

@Component
public class AuthorizationCache implements CacheProviderAware {

	@Override
	public void onCreate(final HazelcastCacheManager cacheManager, final Function<String, CacheConfig<?, ?>> configProfider) {
		cacheManager.createCache("authorizations", configProfider.apply("authorizations"));
	}

}
