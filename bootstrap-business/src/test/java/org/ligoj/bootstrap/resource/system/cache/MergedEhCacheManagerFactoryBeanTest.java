package org.ligoj.bootstrap.resource.system.cache;

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

}
