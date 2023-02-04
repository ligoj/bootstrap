/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import javax.cache.CacheManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.hazelcast.config.CacheConfig;

/**
 * Test class of {@link MergedHazelCastManagerFactoryBean}
 */
class MergedHazelCastManagerFactoryBeanTest {

	private MergedHazelCastManagerFactoryBean bean;

	@BeforeEach
	void prepare() {
		bean = new MergedHazelCastManagerFactoryBean();
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
}
