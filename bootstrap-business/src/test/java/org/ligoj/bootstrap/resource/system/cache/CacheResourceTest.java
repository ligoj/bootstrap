package org.ligoj.bootstrap.resource.system.cache;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test class of {@link CacheResource}
 */
@ExtendWith(SpringExtension.class)
public class CacheResourceTest extends AbstractBootTest {

	@Autowired
	private CacheResource cacheResource;

	@Autowired
	private DummyCacheBean dummyCacheBean;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	public void cleanCache() {
		cacheManager.getCache("test-cache").clear();
	}

	@Test
	public void invalidate() {
		DummyCacheBean.hit = 0;
		cacheResource.invalidate("test-cache");
		Assertions.assertEquals(1, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Assertions.assertEquals(1, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);

		dummyCacheBean.updateHit("entry-key", 99);
		Assertions.assertEquals(99, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(99, dummyCacheBean.getHit("entry-key"));

		cacheResource.invalidate("test-cache");
		Assertions.assertEquals(100, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(100, DummyCacheBean.hit);
		Assertions.assertEquals(100, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(100, DummyCacheBean.hit);
	}

	@Test
	public void getCaches() {
		DummyCacheBean.hit = 0;
		cacheResource.invalidate("test-cache");
		Assertions.assertEquals(1, dummyCacheBean.getHit("entry-key"));
		for (int i = 100000; i-- > 0;) {
			dummyCacheBean.getHit("entry-key" + i);
		}

		final List<CacheStatistics> caches = cacheResource.getCaches();
		Assertions.assertEquals(3, caches.size());
		Assertions.assertTrue(caches.stream().filter(c -> "test-cache".equals(c.getId())).anyMatch(this::assertCache));
	}

	@Test
	public void getCache() {
		dummyCacheBean.getHit("entry-key");
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit("entry-key");
		assertCache(cacheResource.getCache("test-cache"));
	}

	private boolean assertCache(final CacheStatistics cache) {
		Assertions.assertEquals("test-cache", cache.getId());
		Assertions.assertNotNull(cache.getId());
		Assertions.assertTrue(cache.getSize() < 100000);
		Assertions.assertTrue(cache.getSize() > 100);

		// Node check
		final CacheNode node = cache.getNode();
		Assertions.assertNotNull(node.getAddress());
		Assertions.assertNotNull(node.getCluster());
		Assertions.assertNotNull(node.getId());
		Assertions.assertNotNull(node.getVersion());

		// Cluster check
		final CacheCluster cluster = node.getCluster();
		Assertions.assertNotNull(cluster.getId());
		Assertions.assertEquals(1, cluster.getMembers().size());
		Assertions.assertNull(cluster.getMembers().get(0).getCluster());
		Assertions.assertNotNull(cluster.getState());

		// Only with enabled statistics
		Assertions.assertEquals(0, cache.getAverageGetTime());
		Assertions.assertEquals(0, cache.getHitCount());
		Assertions.assertEquals(0, cache.getMissCount());
		Assertions.assertEquals(0, cache.getMissPercentage());
		return true;
	}
}
