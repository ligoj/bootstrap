/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.cache.event.CacheEntryEvent;
import java.util.List;

/**
 * Unit test of {@link CacheGeneration} listener and of {@link CacheEviction} without transaction.
 */
class CacheGenerationTest {

	@Test
	void onChange() {
		final var listener = new CacheGeneration("unit-generation");
		final var generation = CacheGeneration.get("unit-generation");
		final List<CacheEntryEvent<?, ?>> events = List.of(Mockito.mock(CacheEntryEvent.class));

		// No event, no change
		listener.onRemoved(List.of());
		Assertions.assertEquals(generation, CacheGeneration.get("unit-generation"));

		listener.onRemoved(events);
		listener.onUpdated(events);
		listener.onExpired(events);
		Assertions.assertEquals(generation + 3, CacheGeneration.get("unit-generation"));
	}

	@Test
	void evictWithoutTransaction() {
		// Outside a transaction, the eviction is only immediate
		final var manager = Mockito.mock(CacheManager.class);
		final var cache = Mockito.mock(Cache.class);
		Mockito.when(manager.getCache("cache")).thenReturn(cache);
		CacheEviction.clear(manager, "cache");
		CacheEviction.evict(manager, "cache", "key");
		Mockito.verify(cache).clear();
		Mockito.verify(cache).evict("key");
	}
}
