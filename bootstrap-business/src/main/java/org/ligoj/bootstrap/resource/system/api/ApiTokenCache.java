/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.api;

import com.hazelcast.cache.HazelcastCacheManager;
import org.ligoj.bootstrap.resource.system.cache.CacheConfigurer;
import org.ligoj.bootstrap.resource.system.cache.CacheManagerAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * API token cache configuration: the valid token checks, see {@link ApiTokenResource#check(String, String)}. An entry
 * expires {@link #CHECK_DURATION} after its creation, this duration can be overridden with the
 * <code>cache.api-tokens.ttl</code> property, in seconds.
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ApiTokenCache implements CacheManagerAware {

	/**
	 * Default validity of a cached valid check: the maximal delay to see a change not made by
	 * {@link ApiTokenResource}, such as an expiration.
	 */
	static final Duration CHECK_DURATION = new Duration(TimeUnit.MINUTES, 1);

	@Override
	public void onCreate(final HazelcastCacheManager cacheManager, final CacheConfigurer configurer) {
		cacheManager.createCache(ApiTokenResource.CHECK_CACHE, configurer.newCacheConfig(ApiTokenResource.CHECK_CACHE, CHECK_DURATION));
	}

}
