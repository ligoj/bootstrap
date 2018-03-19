package org.ligoj.bootstrap.resource.system.cache;

import javax.cache.CacheManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link MergedHazelCacheManagerFactoryBean}
 */
public class MergedHazelCacheManagerFactoryBeanTest {

	@Test
	public void destroy() {
		final MergedHazelCacheManagerFactoryBean bean = new MergedHazelCacheManagerFactoryBean();
		bean.cacheManager = Mockito.mock(CacheManager.class);
		bean.destroy();
	}

	@Test
	public void getObjectTypeNotInitialized() {
		final MergedHazelCacheManagerFactoryBean bean = new MergedHazelCacheManagerFactoryBean();
		Assertions.assertNull(bean.getObjectType());
	}

	@Test
	public void getObjectType() {
		final MergedHazelCacheManagerFactoryBean bean = new MergedHazelCacheManagerFactoryBean();
		bean.cacheManager = Mockito.mock(CacheManager.class);
		Assertions.assertNotNull(bean.getObjectType());
	}

}
