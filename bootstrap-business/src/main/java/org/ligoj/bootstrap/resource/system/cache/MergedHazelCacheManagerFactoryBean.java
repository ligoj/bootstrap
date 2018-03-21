/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.io.IOException;
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
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionConfig.MaxSizePolicy;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;

/**
 * Factory of {@link EhCacheManagerFactoryBean} with modular {@link CacheConfig} creation delegate to
 * {@link CacheManagerAware} implementors.
 */
public class MergedHazelCacheManagerFactoryBean implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {

	protected CacheManager cacheManager;

	@Autowired
	protected ApplicationContext context;

	@Override
	public void afterPropertiesSet() throws IOException {
		final CachingProvider provider = Caching.getCachingProvider();
		final HazelcastCacheManager cacheManager = (HazelcastCacheManager) provider
				.getCacheManager(URI.create("bootstrap-cache-manager"), null);

		context.getBeansOfType(CacheManagerAware.class)
				.forEach((n, a) -> a.onCreate(cacheManager, this::newCacheConfig));

		final EvictionConfig evictionConfig = new EvictionConfig().setEvictionPolicy(EvictionPolicy.NONE)
				.setMaximumSizePolicy(MaxSizePolicy.ENTRY_COUNT).setSize(5000);

		// Post configuration
		final NearCacheConfig nearCacheConfig = new NearCacheConfig().setInMemoryFormat(InMemoryFormat.OBJECT)
				.setInvalidateOnChange(false).setTimeToLiveSeconds(0).setEvictionConfig(evictionConfig);
		final Config config = cacheManager.getHazelcastInstance().getConfig();
		cacheManager.getCacheNames().forEach(n -> {
			final MapConfig mapConfig = config.getMapConfig(n);
			mapConfig.setNearCacheConfig(nearCacheConfig);
			postConfigure(mapConfig);
		});

		this.cacheManager = cacheManager;
	}

	/**
	 * Compete the configuration after its creation and configuration be {@link CacheManagerAware} implementor.
	 */
	protected void postConfigure(MapConfig mapConfig) {
		if (System.getProperty("java.security.policy") != null) {
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