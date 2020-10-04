/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.util.Collections;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;

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
	void expirityModifyPolicy() throws InterruptedException {
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
	void expirityTouchedPolicy() throws InterruptedException {
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
		Assertions.assertTrue(caches.stream().filter(c -> "test-cache".equals(c.getId())).anyMatch(this::assertCache));
	}

	private void doManyHits() {
		for (var i = 100000; i-- > 0;) {
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
	}

	@Test
	void getCacheNotExists() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> assertCache(cacheResource.getCache("NOT-EXISTS")));
	}

	@Test
	void setStatistics() {
		dummyCacheBean.getHit("entry-key");
		cacheResource.invalidate("test-cache");
		dummyCacheBean.getHit("entry-key");
		doManyHits();

		final var policy = System.getProperty("java.security.policy");
		try {
			System.setProperty("java.security.policy", "path_to_policy");
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
		} finally {
			// Restore policy
			if (policy == null) {
				System.clearProperty("java.security.policy");
			} else {
				System.setProperty("java.security.policy", policy);
			}
		}
	}

	private boolean assertCache(final CacheStatistics cache) {
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

		// Only with enabled statistics
		Assertions.assertNull(cache.getAverageGetTime());
		Assertions.assertNull(cache.getHitCount());
		Assertions.assertNull(cache.getMissCount());
		Assertions.assertNull(cache.getHitPercentage());
		Assertions.assertNull(cache.getMissPercentage());
		return true;
	}

	@Test
	void onApplicationEventNotRunning() {
		final var event = Mockito.mock(ContextClosedEvent.class);
		final var resource = new CacheResource();
		resource.cacheManager = Mockito.mock(CacheManager.class);

		Mockito.when(resource.cacheManager.getCacheNames()).thenReturn(Collections.singletonList("my-cache"));
		final var cache = Mockito.mock(Cache.class);
		Mockito.when(resource.cacheManager.getCache("my-cache")).thenReturn(cache);
		final var node = Mockito.mock(NodeEngine.class);
		final var rService = Mockito.mock(RemoteService.class);
		Mockito.when(node.isRunning()).thenReturn(true);
		final AbstractDistributedObject<RemoteService> cacheProxy = new AbstractDistributedObject<>(node, rService) {

			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getServiceName() {
				return null;
			}
		};
		Mockito.when(cache.getNativeCache()).thenReturn(cacheProxy);
		Mockito.doThrow(new HazelcastInstanceNotActiveException()).when(node).getHazelcastInstance();
		resource.onApplicationEvent(event);
	}

	@Test
	void onApplicationEventRunning() {
		final var event = Mockito.mock(ContextClosedEvent.class);
		final var resource = new CacheResource();
		resource.cacheManager = Mockito.mock(CacheManager.class);

		Mockito.when(resource.cacheManager.getCacheNames()).thenReturn(Collections.singletonList("my-cache"));
		final var cache = Mockito.mock(Cache.class);
		Mockito.when(resource.cacheManager.getCache("my-cache")).thenReturn(cache);
		final var node = Mockito.mock(NodeEngine.class);
		final var rService = Mockito.mock(RemoteService.class);
		Mockito.when(node.isRunning()).thenReturn(true);
		final AbstractDistributedObject<RemoteService> cacheProxy = new AbstractDistributedObject<>(node, rService) {

			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getServiceName() {
				return null;
			}
		};
		Mockito.when(cache.getNativeCache()).thenReturn(cacheProxy);
		final var instance = Mockito.mock(HazelcastInstance.class);
		Mockito.when(node.getHazelcastInstance()).thenReturn(instance);
		final var service = Mockito.mock(LifecycleService.class);
		Mockito.when(service.isRunning()).thenReturn(true);
		Mockito.when(instance.getLifecycleService()).thenReturn(service);
		resource.onApplicationEvent(event);
		Mockito.verify(service).terminate();

	}
}
