package org.ligoj.bootstrap.resource.system.cache;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.transaction.Transactional;

import org.ligoj.bootstrap.AbstractSecurityTest;

/**
 * Test class of {@link CacheResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/jpa-context-test.xml", "classpath:/META-INF/spring/business-context-test.xml" })
@Rollback
@Transactional
public class CacheResourceTest extends AbstractSecurityTest {

	@Autowired
	private CacheResource cacheResource;

	@Autowired
	private DummyCacheBean dummyCacheBean;

	@Test
	public void invalidate() {
		DummyCacheBean.hit = 0;
		Assert.assertEquals(1, dummyCacheBean.getHit());
		Assert.assertEquals(1, DummyCacheBean.hit);
		Assert.assertEquals(1, dummyCacheBean.getHit());
		Assert.assertEquals(1, DummyCacheBean.hit);
		cacheResource.invalidate("test-cache");
		Assert.assertEquals(2, dummyCacheBean.getHit());
		Assert.assertEquals(2, DummyCacheBean.hit);
		Assert.assertEquals(2, dummyCacheBean.getHit());
		Assert.assertEquals(2, DummyCacheBean.hit);
	}

	@Test
	public void getCaches() {
		dummyCacheBean.getHit();
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit();
		Assert.assertEquals(2, cacheResource.getCaches().size());
		for (final CacheStatistics cache : cacheResource.getCaches()) {
			if (cache.getName().equals("test-cache")) {
				assertCache(cache);
				return;
			}
		}
		Assert.fail("'test-cache' cache not found");
	}

	@Test
	public void getCache() {
		dummyCacheBean.getHit();
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit();
		assertCache(cacheResource.getCache("test-cache"));
	}

	private void assertCache(final CacheStatistics cache) {
		Assert.assertEquals("test-cache", cache.getName());
		Assert.assertNotNull(cache.getId());
		Assert.assertTrue(cache.getBytes() > 0);
		Assert.assertTrue(cache.getHitCount() >= 1);
		Assert.assertTrue(cache.getMissCount() >= 1);
		Assert.assertTrue(cache.getOffHeapBytes() == 0);
		Assert.assertTrue(cache.getSize() == 1);
	}
}
