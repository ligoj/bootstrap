/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.EvictionConfig;
import org.ligoj.bootstrap.resource.system.cache.CacheConfigurer;
import org.ligoj.bootstrap.resource.system.cache.CacheManagerAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import javax.cache.expiry.Duration;

/**
 * Authorizations and user details cache configuration.
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AuthorizationCache implements CacheManagerAware {

	@Override
	public void onCreate(final HazelcastCacheManager cacheManager, final CacheConfigurer configurer) {
		cacheManager.createCache("authorizations", configurer.newCacheConfig("authorizations"));
		final var details = configurer.newCacheConfig("user-details", Duration.ONE_HOUR);
		details.setEvictionConfig(new EvictionConfig());
		cacheManager.createCache("user-details", details);
	}

}
