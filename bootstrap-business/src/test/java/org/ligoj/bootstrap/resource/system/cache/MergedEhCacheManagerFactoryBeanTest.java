package org.ligoj.bootstrap.resource.system.cache;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test class of {@link MergedEhCacheManagerFactoryBean}
 */
public class MergedEhCacheManagerFactoryBeanTest {

	@Test
	public void destroy() {
		final MergedEhCacheManagerFactoryBean bean = new MergedEhCacheManagerFactoryBean();
		bean.cacheManager = Mockito.mock(net.sf.ehcache.CacheManager.class);
		bean.destroy();
	}

	@Test
	public void getObjectTypeNotInitialized() {
		final MergedEhCacheManagerFactoryBean bean = new MergedEhCacheManagerFactoryBean();
		Assert.assertNull(bean.getObjectType());
	}

	@Test
	public void getObjectType() {
		final MergedEhCacheManagerFactoryBean bean = new MergedEhCacheManagerFactoryBean();
		bean.cacheManager = Mockito.mock(net.sf.ehcache.CacheManager.class);
		Assert.assertNotNull(bean.getObjectType());
	}

}
