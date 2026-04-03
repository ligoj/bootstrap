/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.configuration;

import com.hazelcast.cache.HazelcastCacheManager;
import org.ligoj.bootstrap.resource.system.cache.CacheConfigurer;
import org.ligoj.bootstrap.resource.system.cache.CacheManagerAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

/**
 * "Configuration" values cache configuration.
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ConfigurationCache implements CacheManagerAware {

	@Override
	public void onCreate(HazelcastCacheManager cacheManager, final CacheConfigurer configurer) {
		cacheManager.createCache("configuration", configurer.newCacheConfig("configuration"));
		cacheManager.createCache("hooks", configurer.newCacheConfig("hooks"));
	}

}
