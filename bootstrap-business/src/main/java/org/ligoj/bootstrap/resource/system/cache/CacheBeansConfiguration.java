/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import javax.cache.CacheManager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Cache infrastructure beans, formerly declared in {@code business-context-common.xml}.
 * <p>
 * They are {@link Role @Role(ROLE_INFRASTRUCTURE)} because Spring Boot's actuator {@code meterRegistryPostProcessor}
 * (cache metrics binding) instantiates this chain — {@code hazelcast}, its produced JCache manager, the Spring
 * {@code cacheManager} facade and every {@link CacheManagerAware} contributor — while the {@code BeanPostProcessor}s
 * are still being registered. These beans carry no proxied behavior (no {@code @Transactional} / AOP), so skipping
 * post-processing is intended; the role declaration states it and silences the startup
 * "not eligible for getting processed by all BeanPostProcessors" warnings. Both methods are {@code static} so the
 * configuration class itself needs no early instantiation either.
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class CacheBeansConfiguration {

	/**
	 * The merged Hazelcast JCache manager, fed by all {@link CacheManagerAware} beans.
	 *
	 * @param location Hazelcast XML configuration location ({@code cache.location} property).
	 * @return the factory producing the JCache {@link CacheManager}.
	 */
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static MergedHazelCastManagerFactoryBean hazelcast(
			@Value("${cache.location:classpath:META-INF/hazelcast-local.xml}") final String location) {
		final var factory = new MergedHazelCastManagerFactoryBean();
		factory.setLocation(location);
		return factory;
	}

	/**
	 * The Spring cache abstraction facade over the JCache manager, consumed by {@code <cache:annotation-driven/>}.
	 *
	 * @param hazelcast The JCache manager produced by {@link #hazelcast(String)}.
	 * @return the Spring {@code cacheManager} bean.
	 */
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static JCacheCacheManager cacheManager(@Qualifier("hazelcast") final CacheManager hazelcast) {
		return new JCacheCacheManager(hazelcast);
	}
}
