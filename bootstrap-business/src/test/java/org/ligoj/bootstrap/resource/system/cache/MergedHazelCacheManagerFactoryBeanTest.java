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
 * Test class of {@link MergedHazelCacheManagerFactoryBean}
 */
public class MergedHazelCacheManagerFactoryBeanTest {

	private MergedHazelCacheManagerFactoryBean bean;
	private String oldPolicy;

	@BeforeEach
	public void prepare() {
		bean = new MergedHazelCacheManagerFactoryBean();
		oldPolicy = System.getProperty("java.security.policy");
	}

	private void clean() {
		if (oldPolicy == null) {
			oldPolicy = System.getProperty("java.security.policy");
		} else {
			System.clearProperty("java.security.policy");
		}
	}

	@Test
	public void destroy() {
		bean.cacheManager = Mockito.mock(CacheManager.class);
		bean.destroy();
	}

	@Test
	public void getObjectTypeNotInitialized() {
		// JHCache instance, not Spring
		Assertions.assertEquals(CacheManager.class, bean.getObjectType());
	}

	@Test
	public void getObjectType() {
		bean.cacheManager = Mockito.mock(CacheManager.class);

		// JHCache instance, not Spring
		Assertions.assertNotNull(bean.getObjectType());
	}

	@Test
	public void postConfigureNoPolicy() {
		final CacheConfig<?,?> mapConfig = Mockito.mock(CacheConfig.class);
		try {
			System.clearProperty("java.security.policy");
			bean.postConfigure(mapConfig);
		} finally {
			clean();
		}
		Mockito.verify(mapConfig, Mockito.never()).setStatisticsEnabled(true);
	}

	@Test
	public void postConfigurePolicy() {
		final CacheConfig<?,?> mapConfig = Mockito.mock(CacheConfig.class);
		try {
			System.setProperty("java.security.policy", "some_path_to_policy");
			bean.postConfigure(mapConfig);
		} finally {
			clean();
		}
		// JMX enabled
		Mockito.verify(mapConfig).setStatisticsEnabled(true);
	}

}
