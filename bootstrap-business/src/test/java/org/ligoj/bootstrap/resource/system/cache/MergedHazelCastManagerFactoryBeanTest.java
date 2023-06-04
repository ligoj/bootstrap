/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import com.hazelcast.config.CacheConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.mockito.Mockito;

import javax.cache.CacheManager;

/**
 * Test class of {@link MergedHazelCastManagerFactoryBean}
 */
class MergedHazelCastManagerFactoryBeanTest {

	private MergedHazelCastManagerFactoryBean bean;

	@BeforeEach
	void prepare() {
		bean = new MergedHazelCastManagerFactoryBean();
		bean.configuration = Mockito.mock(ConfigurationResource.class);
	}

	@Test
	void destroy() {
		bean.cacheManager = Mockito.mock(CacheManager.class);
		bean.destroy();
	}

	@Test
	void getObjectTypeNotInitialized() {
		// JHCache instance, not Spring
		Assertions.assertEquals(CacheManager.class, bean.getObjectType());
	}

	@Test
	void getObjectType() {
		bean.cacheManager = Mockito.mock(CacheManager.class);

		// JHCache instance, not Spring
		Assertions.assertNotNull(bean.getObjectType());
	}

	@Test
	void postConfigureNoPolicy() {
		final CacheConfig<?, ?> mapConfig = Mockito.mock(CacheConfig.class);
		bean.postConfigure(mapConfig);
		Mockito.verify(mapConfig, Mockito.never()).setStatisticsEnabled(true);
	}

	@Test
	void postConfigureWithoutStatistics() {
		final CacheConfig<?, ?> mapConfig = Mockito.mock(CacheConfig.class);
		bean.setStatisticsEnabled(false);
		bean.postConfigure(mapConfig);
		Mockito.verify(mapConfig, Mockito.never()).setStatisticsEnabled(true);
	}

	@Test
	void postConfigureWithStatistics() {
		final CacheConfig<?, ?> mapConfig = Mockito.mock(CacheConfig.class);
		bean.setStatisticsEnabled(true);
		bean.postConfigure(mapConfig);
		Mockito.verify(mapConfig, Mockito.times(1)).setStatisticsEnabled(true);
	}

	@Test
	void newCacheConfigNoTTL() {
		Mockito.when(bean.configuration.get("cache.test.ttl", -1)).thenReturn(-1);
		final var config = bean.newCacheConfig("test");
		Assertions.assertTrue(config.getExpiryPolicyFactory().create().getExpiryForUpdate().isEternal());
	}

	@Test
	void newCacheConfigTTL() {
		Mockito.when(bean.configuration.get("cache.test.ttl", -1)).thenReturn(3600);
		final var config = bean.newCacheConfig("test");
		Assertions.assertFalse(config.getExpiryPolicyFactory().create().getExpiryForUpdate().isEternal());
		Assertions.assertEquals(3600, config.getExpiryPolicyFactory().create().getExpiryForUpdate().getDurationAmount());
	}

}
