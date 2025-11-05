/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.security;

import static java.util.concurrent.TimeUnit.HOURS;

import java.util.function.Function;

import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;

import org.ligoj.bootstrap.resource.system.cache.CacheManagerAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.EvictionConfig;

/**
 * Authorizations and user details cache configuration.
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AuthorizationCache implements CacheManagerAware {

	@Override
	public void onCreate(final HazelcastCacheManager cacheManager, final Function<String, CacheConfig<?, ?>> provider) {
		cacheManager.createCache("authorizations", provider.apply("authorizations"));
		final var details = provider.apply("user-details");
		details.setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(HOURS, 1)));
		details.setEvictionConfig(new EvictionConfig());
		cacheManager.createCache("user-details", details);
	}

}
