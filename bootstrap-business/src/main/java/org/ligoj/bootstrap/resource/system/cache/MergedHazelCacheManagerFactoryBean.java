/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.net.URI;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.ApplicationContext;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;

/**
 * Factory of {@link EhCacheManagerFactoryBean} with modular {@link CacheConfig} creation delegate to
 * {@link CacheManagerAware} implementors.
 */
public class MergedHazelCacheManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {

	protected CacheManager cacheManager;

	@Autowired
	protected ApplicationContext context;

	@Override
	public void afterPropertiesSet() {
		final CachingProvider provider = Caching.getCachingProvider();
		final HazelcastCacheManager cacheManager = (HazelcastCacheManager) provider
				.getCacheManager(URI.create("bootstrap-cache-manager"), null);
		context.getBeansOfType(CacheManagerAware.class)
				.forEach((n, a) -> a.onCreate(cacheManager, this::newCacheConfig));
		this.cacheManager = cacheManager;
	}

	/**
	 * Compete the configuration after its creation and configuration be {@link CacheManagerAware} implementor.
	 */
	protected void postConfigure(final CacheConfig<?, ?> mapConfig) {
		if (CacheResource.isStatisticEnabled()) {
			// When a policy is defined, assume JMX is enabled
			mapConfig.setStatisticsEnabled(true);
		}
	}

	/**
	 * Create a new {@link CacheConfig} with configured settings before {@link CacheManagerAware} implementor.
	 */
	private CacheConfig<?, ?> newCacheConfig(final String name) {
		final CacheConfig<?, ?> config = new CacheConfig<>(name);
		config.setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU)
				.setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.ENTRY_COUNT));
		config.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(Duration.ETERNAL));
		// Post configuration
		postConfigure(config);
		return config;
	}

	@Override
	public CacheManager getObject() {
		return this.cacheManager;
	}

	@Override
	public Class<? extends CacheManager> getObjectType() {
		return this.cacheManager == null ? CacheManager.class : this.cacheManager.getClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() {
		this.cacheManager.close();
	}

}