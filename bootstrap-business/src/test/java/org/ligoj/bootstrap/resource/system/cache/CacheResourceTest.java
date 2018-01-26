package org.ligoj.bootstrap.resource.system.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.sf.ehcache.CacheManager;

/**
 * Test class of {@link CacheResource}
 */
@ExtendWith(SpringExtension.class)
public class CacheResourceTest extends AbstractBootTest {

	@Autowired
	private CacheResource cacheResource;

	@Autowired
	private DummyCacheBean dummyCacheBean;

	@BeforeEach
	public void cleanCache() {
		CacheManager.getInstance().getCache("test-cache").removeAll();
	}

	@Test
	public void invalidate() {
		DummyCacheBean.hit = 0;
		Assertions.assertEquals(1, dummyCacheBean.getHit());
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Assertions.assertEquals(1, dummyCacheBean.getHit());
		Assertions.assertEquals(1, DummyCacheBean.hit);
		cacheResource.invalidate("test-cache");
		Assertions.assertEquals(2, dummyCacheBean.getHit());
		Assertions.assertEquals(2, DummyCacheBean.hit);
		Assertions.assertEquals(2, dummyCacheBean.getHit());
		Assertions.assertEquals(2, DummyCacheBean.hit);
	}

	@Test
	public void getCaches() {
		dummyCacheBean.getHit();
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit();
		Assertions.assertTrue(cacheResource.getCaches().size() >= 1);
		Assertions.assertTrue(cacheResource.getCaches().stream().filter(c -> "test-cache".equals(c.getName())).anyMatch(this::assertCache));
	}

	@Test
	public void getCache() {
		dummyCacheBean.getHit();
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit();
		assertCache(cacheResource.getCache("test-cache"));
	}

	private boolean assertCache(final CacheStatistics cache) {
		Assertions.assertEquals("test-cache", cache.getName());
		Assertions.assertNotNull(cache.getId());
		Assertions.assertTrue(cache.getBytes() > 0);
		Assertions.assertTrue(cache.getHitCount() >= 1);
		Assertions.assertTrue(cache.getMissCount() >= 1);
		Assertions.assertTrue(cache.getOffHeapBytes() == 0);
		Assertions.assertTrue(cache.getSize() == 1);
		return true;
	}
}
