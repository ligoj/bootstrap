/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import com.hazelcast.config.CacheConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.cache.CacheManager;

/**
 * Test class of {@link MergedHazelCastManagerFactoryBean}
 */
class MergedHazelCastManagerFactoryBeanTest {

	private MergedHazelCastManagerFactoryBean bean;

	@BeforeEach
	void prepare() {
		bean = new MergedHazelCastManagerFactoryBean();
		bean.env = Mockito.mock(ConfigurableEnvironment.class);
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

	@SuppressWarnings("unchecked")
	private CacheConfig<String, Object> newMockConfig() {
		return Mockito.mock(CacheConfig.class);
	}

	@Test
	void postConfigureNoPolicy() {
		final CacheConfig<String, Object> mapConfig = newMockConfig();
		bean.postConfigure(mapConfig);
		Mockito.verify(mapConfig, Mockito.never()).setStatisticsEnabled(true);
	}

	@Test
	void postConfigureWithoutStatistics() {
		final CacheConfig<String, Object> mapConfig = newMockConfig();
		bean.setStatisticsEnabled(false);
		bean.postConfigure(mapConfig);
		Mockito.verify(mapConfig, Mockito.never()).setStatisticsEnabled(true);
	}

	@Test
	void postConfigureWithStatistics() {
		final CacheConfig<String, Object> mapConfig = newMockConfig();
		bean.setStatisticsEnabled(true);
		bean.postConfigure(mapConfig);
		Mockito.verify(mapConfig, Mockito.times(1)).setStatisticsEnabled(true);
	}

	@Test
	void newCacheConfigNoTTL() {
		Mockito.when(bean.env.getProperty("cache.test.ttl")).thenReturn("-1");
		final var config = bean.newCacheConfig("test");
		Assertions.assertTrue(config.getExpiryPolicyFactory().create().getExpiryForUpdate().isEternal());
	}

	@Test
	void newCacheConfigTTL() {
		Mockito.when(bean.env.getProperty("cache.test.ttl")).thenReturn("3600");
		final var config = bean.newCacheConfig("test");
		Assertions.assertFalse(config.getExpiryPolicyFactory().create().getExpiryForUpdate().isEternal());
		Assertions.assertEquals(3600, config.getExpiryPolicyFactory().create().getExpiryForUpdate().getDurationAmount());
	}

}
