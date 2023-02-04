/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.LifecycleService;

/**
 * Test class of {@link CacheResource}
 */
@ExtendWith(SpringExtension.class)
class CacheResourceTest extends AbstractBootTest {

	@Autowired
	private CacheResource cacheResource;

	@Autowired
	private DummyCacheBean dummyCacheBean;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void cleanCache() {
		cacheManager.getCache("test-cache").clear();
		cacheResource.enableStatistics();
	}

	@Test
	void invalidate() {
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
	void invalidateAll() {
		DummyCacheBean.hit = 0;
		cacheResource.invalidate();
		Assertions.assertEquals(1, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(1, dummyCacheBean.getHit("entry-key"));

		dummyCacheBean.updateHit("entry-key", 99);
		Assertions.assertEquals(99, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(99, dummyCacheBean.getHit("entry-key"));

		cacheResource.invalidate();
		Assertions.assertEquals(100, dummyCacheBean.getHit("entry-key"));
		Assertions.assertEquals(100, dummyCacheBean.getHit("entry-key"));
	}

	/**
	 * Tested cache expires with 1 second after the last update.
	 */
	@Test
	void expiryModifyPolicy() throws InterruptedException {
		DummyCacheBean.hit = 0;
		cacheResource.invalidate("test-cache-1");
		Assertions.assertEquals(1, dummyCacheBean.getHit1("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Assertions.assertEquals(1, dummyCacheBean.getHit1("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Thread.sleep(800);
		Assertions.assertEquals(1, dummyCacheBean.getHit1("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Thread.sleep(800);

		// Last update was 1.6s ago
		Assertions.assertEquals(2, dummyCacheBean.getHit1("entry-key"));
		Assertions.assertEquals(2, DummyCacheBean.hit);
		Thread.sleep(800);
		Assertions.assertEquals(2, dummyCacheBean.getHit1("entry-key"));
		Assertions.assertEquals(2, DummyCacheBean.hit);
		dummyCacheBean.updateHit1("entry-key", 9);
		Assertions.assertEquals(9, DummyCacheBean.hit);
		Thread.sleep(800);
		Assertions.assertEquals(9, dummyCacheBean.getHit1("entry-key"));
		Assertions.assertEquals(9, DummyCacheBean.hit);
	}

	/**
	 * Tested cache expires with 1 second after the last update/access.
	 */
	@Test
	void expiryTouchedPolicy() throws InterruptedException {
		DummyCacheBean.hit = 0;
		cacheResource.invalidate("test-cache-2");
		Assertions.assertEquals(1, dummyCacheBean.getHit2("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Assertions.assertEquals(1, dummyCacheBean.getHit2("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Thread.sleep(800);
		Assertions.assertEquals(1, dummyCacheBean.getHit2("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Thread.sleep(800);

		// Last access was 800ms ago
		Assertions.assertEquals(1, dummyCacheBean.getHit2("entry-key"));
		Assertions.assertEquals(1, DummyCacheBean.hit);
		Thread.sleep(1600);

		// Last access was 1600ms ago
		Assertions.assertEquals(2, dummyCacheBean.getHit2("entry-key"));
		Assertions.assertEquals(2, DummyCacheBean.hit);
		Thread.sleep(800);
		dummyCacheBean.updateHit2("entry-key", 9);
		Assertions.assertEquals(9, DummyCacheBean.hit);
		Thread.sleep(800);
		// Last update was 800ms ago
		Assertions.assertEquals(9, dummyCacheBean.getHit2("entry-key"));
		Assertions.assertEquals(9, DummyCacheBean.hit);
	}

	@Test
	void getCaches() {
		DummyCacheBean.hit = 0;
		cacheResource.invalidate("test-cache");
		Assertions.assertEquals(1, dummyCacheBean.getHit("entry-key"));
		doManyHits();

		final var caches = cacheResource.getCaches();
		Assertions.assertEquals(6, caches.size());
		caches.stream().filter(c -> "test-cache".equals(c.getId())).forEach(this::assertCache);
	}

	private void doManyHits() {
		for (var i = 100000; i-- > 0; ) {
			dummyCacheBean.getHit("entry-key" + i);
		}
	}

	@Test
	void getCache() {
		dummyCacheBean.getHit("entry-key");
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit("entry-key");
		doManyHits();
		assertCache(cacheResource.getCache("test-cache"));

		cacheResource.disableStatistics();
		doManyHits();
		Assertions.assertEquals(0, cacheResource.getCache("test-cache").getSize());
	}

	@Test
	void getCacheNotExists() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> cacheResource.getCache("NOT-EXISTS"));
	}

	@Test
	void setStatistics() {

		final var result = new CacheStatistics();
		final var statistics = Mockito.mock(com.hazelcast.cache.CacheStatistics.class);
		Mockito.doReturn(1.1F).when(statistics).getCacheMissPercentage();
		Mockito.doReturn(2.2F).when(statistics).getCacheHitPercentage();
		Mockito.doReturn(3L).when(statistics).getCacheHits();
		Mockito.doReturn(4L).when(statistics).getCacheMisses();
		Mockito.doReturn(5F).when(statistics).getAverageGetTime();
		cacheResource.setStatistics(result, statistics);

		// Check results
		Assertions.assertEquals(1.1, result.getMissPercentage(), 0.01);
		Assertions.assertEquals(2.2, result.getHitPercentage(), 0.01);
		Assertions.assertEquals(3, result.getHitCount().longValue());
		Assertions.assertEquals(4, result.getMissCount().longValue());
		Assertions.assertEquals(5, result.getAverageGetTime(), 0.01);
	}

	private void assertCache(final CacheStatistics cache) {
		Assertions.assertEquals("test-cache", cache.getId());
		Assertions.assertNotNull(cache.getId());
		Assertions.assertTrue(cache.getSize() < 100000);
		Assertions.assertTrue(cache.getSize() > 100);

		// Node check
		final var node = cache.getNode();
		Assertions.assertNotNull(node.getAddress());
		Assertions.assertNotNull(node.getCluster());
		Assertions.assertNotNull(node.getId());
		Assertions.assertNotNull(node.getVersion());

		// Cluster check
		final var cluster = node.getCluster();
		Assertions.assertNotNull(cluster.getId());
		Assertions.assertEquals(1, cluster.getMembers().size());
		Assertions.assertNull(cluster.getMembers().get(0).getCluster());
		Assertions.assertNotNull(cluster.getState());
	}

	@Test
	void onApplicationEventNotRunning() {
		final var event = Mockito.mock(ContextClosedEvent.class);
		final var resource = new CacheResource();
		final var cacheManager = Mockito.mock(JCacheCacheManager.class);
		resource.cacheManager = cacheManager;
		final var cache = Mockito.mock(HazelcastCacheManager.class);
		Mockito.when(cacheManager.getCacheManager()).thenReturn(cache);
		Mockito.doThrow(new HazelcastInstanceNotActiveException()).when(cache).getHazelcastInstance();
		resource.onApplicationEvent(event);
	}

	@Test
	void onApplicationEventRunning() {
		final var event = Mockito.mock(ContextClosedEvent.class);
		final var resource = new CacheResource();
		final var cacheManager = Mockito.mock(JCacheCacheManager.class);
		resource.cacheManager = cacheManager;
		final var cache = Mockito.mock(HazelcastCacheManager.class);
		Mockito.when(cacheManager.getCacheManager()).thenReturn(cache);

		final var instance = Mockito.mock(HazelcastInstance.class);
		Mockito.when(cache.getHazelcastInstance()).thenReturn(instance);
		final var service = Mockito.mock(LifecycleService.class);
		Mockito.when(service.isRunning()).thenReturn(true);
		Mockito.when(instance.getLifecycleService()).thenReturn(service);
		resource.onApplicationEvent(event);
		Mockito.verify(service).terminate();

	}
}
