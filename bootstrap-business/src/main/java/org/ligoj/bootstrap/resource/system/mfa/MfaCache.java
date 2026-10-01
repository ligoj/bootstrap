/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.mfa;

import com.hazelcast.cache.HazelcastCacheManager;
import org.ligoj.bootstrap.resource.system.cache.CacheConfigurer;
import org.ligoj.bootstrap.resource.system.cache.CacheManagerAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * MFA cache configuration, durations can be overridden with the <code>cache.[name].ttl</code> property, in seconds:
 * <ul>
 * <li><code>mfa-attempts</code>: the consecutive failed code verifications per user. An entry expires
 * {@link #LOCK_DURATION} after the last failure, which also ends the lock.</li>
 * <li><code>mfa-verified</code>: the last successful verification per user, until the next authentication or
 * {@link #VERIFIED_DURATION}.</li>
 * </ul>
 */
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class MfaCache implements CacheManagerAware {

	/**
	 * Default lock duration after the last failed verification.
	 */
	static final Duration LOCK_DURATION = new Duration(TimeUnit.MINUTES, 15);

	/**
	 * Default validity of a successful verification, unless a new authentication happens before.
	 */
	static final Duration VERIFIED_DURATION = new Duration(TimeUnit.HOURS, 12);

	@Override
	public void onCreate(final HazelcastCacheManager cacheManager, final CacheConfigurer configurer) {
		cacheManager.createCache(MfaResource.ATTEMPTS_CACHE, configurer.newCacheConfig(MfaResource.ATTEMPTS_CACHE, LOCK_DURATION));
		cacheManager.createCache(MfaResource.VERIFIED_CACHE, configurer.newCacheConfig(MfaResource.VERIFIED_CACHE, VERIFIED_DURATION));
	}

}
