package org.ligoj.bootstrap.resource.system.cache;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ligoj.bootstrap.AbstractSecurityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.sf.ehcache.CacheManager;

/**
 * Test class of {@link CacheResource}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
public class CacheResourceTest extends AbstractSecurityTest {

	@Autowired
	private CacheResource cacheResource;

	@Autowired
	private DummyCacheBean dummyCacheBean;

	@Before
	public void cleanCache() {
		CacheManager.getInstance().getCache("test-cache").removeAll();
	}

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
		Assert.assertTrue(cacheResource.getCaches().size() >= 1);
		Assert.assertTrue(cacheResource.getCaches().stream().filter(c -> "test-cache".equals(c.getName())).anyMatch(this::assertCache));
	}

	@Test
	public void getCache() {
		dummyCacheBean.getHit();
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit();
		assertCache(cacheResource.getCache("test-cache"));
	}

	private boolean assertCache(final CacheStatistics cache) {
		Assert.assertEquals("test-cache", cache.getName());
		Assert.assertNotNull(cache.getId());
		Assert.assertTrue(cache.getBytes() > 0);
		Assert.assertTrue(cache.getHitCount() >= 1);
		Assert.assertTrue(cache.getMissCount() >= 1);
		Assert.assertTrue(cache.getOffHeapBytes() == 0);
		Assert.assertTrue(cache.getSize() == 1);
		return true;
	}
}
