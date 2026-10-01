/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.ligoj.bootstrap.resource.system.security.AuthorizationResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test class of {@link LocalCacheSnapshot} and {@link CacheGeneration}.
 */
@ExtendWith(SpringExtension.class)
class LocalCacheSnapshotTest extends AbstractBootTest {

	@Autowired
	private AuthorizationResource authorizationResource;

	@Autowired
	private CacheResource cacheResource;

	@Test
	void get() {
		final var snapshot = new LocalCacheSnapshot<String>("test-snapshot");
		final var loads = new AtomicInteger();

		// Loaded once while the generation is unchanged
		Assertions.assertEquals("v1", snapshot.get(() -> "v" + loads.incrementAndGet()));
		Assertions.assertEquals("v1", snapshot.get(() -> "v" + loads.incrementAndGet()));
		Assertions.assertEquals(1, loads.get());

		// Reloaded after a change of the cache
		CacheGeneration.increment("test-snapshot");
		Assertions.assertEquals("v2", snapshot.get(() -> "v" + loads.incrementAndGet()));
		Assertions.assertEquals("v2", snapshot.get(() -> "v" + loads.incrementAndGet()));
	}

	@Test
	void cachedResourcesAreProxied() {
		// An early injection, before the cache and transaction proxies, would silently disable the cache
		Assertions.assertTrue(org.springframework.aop.support.AopUtils.isAopProxy(authorizationResource));
	}

	@Test
	void generationOnInvalidate() {
		// A removal of a cluster cache entry changes the generation of this node
		authorizationResource.getAuthorizations();
		final var generation = CacheGeneration.get("authorizations");
		cacheResource.invalidate("authorizations");
		Assertions.assertTrue(CacheGeneration.get("authorizations") > generation);
	}

	@Test
	void generationOnInvalidateHooks() {
		cacheManager.getCache("hooks").put("key", "value");
		final var generation = CacheGeneration.get("hooks");
		cacheResource.invalidate();
		Assertions.assertTrue(CacheGeneration.get("hooks") > generation);
	}
}
