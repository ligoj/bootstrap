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
	private String oldPolicy;

	@BeforeEach
	void prepare() {
		bean = new MergedHazelCastManagerFactoryBean();
		oldPolicy = System.getProperty("java.security.policy");
	}

	private void clean() {
		if (oldPolicy == null) {
			System.clearProperty("java.security.policy");
		} else {
			System.setProperty("java.security.policy", oldPolicy);
		}
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
	void postConfigurePolicy() {
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
