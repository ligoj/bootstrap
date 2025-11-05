/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import java.util.function.Function;

import org.ligoj.bootstrap.resource.system.cache.CacheManagerAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;

/**
 * "Configuration" values cache configuration.
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ConfigurationCache implements CacheManagerAware {

	@Override
	public void onCreate(HazelcastCacheManager cacheManager, final Function<String, CacheConfig<?, ?>> provider) {
		cacheManager.createCache("configuration", provider.apply("configuration"));
		cacheManager.createCache("hooks", provider.apply("hooks"));
	}

}
