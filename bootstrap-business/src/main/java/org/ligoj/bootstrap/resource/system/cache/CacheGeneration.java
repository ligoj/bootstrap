/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import com.hazelcast.config.CacheConfig;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local generation of a cluster cache: a counter of this node, incremented when an entry of the cache is removed,
 * updated or expired, whatever the node performing the change. A node can then keep a local snapshot of a cached
 * value, valid while the generation is unchanged, see {@link LocalCacheSnapshot}.
 * <p>
 * The listener is synchronous: a removal returns once the generation of every node is incremented. All the Spring and
 * JCache evictions (<code>@CacheRemoveAll</code>, {@link org.springframework.cache.Cache#clear()},
 * {@link javax.cache.Cache#removeAll()}) notify it, but not {@link javax.cache.Cache#clear()}.
 */
public final class CacheGeneration implements CacheEntryRemovedListener<Object, Object>,
		CacheEntryUpdatedListener<Object, Object>, CacheEntryExpiredListener<Object, Object>, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Map<String, AtomicLong> GENERATIONS = new ConcurrentHashMap<>();

	/**
	 * The watched cache name.
	 */
	private final String cache;

	CacheGeneration(final String cache) {
		this.cache = cache;
	}

	/**
	 * Watch the changes of the cache configured by the given configuration.
	 *
	 * @param config The cache configuration, before the creation of the cache.
	 */
	public static void watch(final CacheConfig<String, Object> config) {
		config.addCacheEntryListenerConfiguration(new MutableCacheEntryListenerConfiguration<>(
				FactoryBuilder.factoryOf(new CacheGeneration(config.getName())), null, false, true));
	}

	/**
	 * Return the current local generation of the given cache.
	 *
	 * @param cache The cache name.
	 * @return The current generation.
	 */
	public static long get(final String cache) {
		return counter(cache).get();
	}

	/**
	 * Increment the local generation of the given cache.
	 *
	 * @param cache The cache name.
	 */
	static void increment(final String cache) {
		counter(cache).incrementAndGet();
	}

	private static AtomicLong counter(final String cache) {
		return GENERATIONS.computeIfAbsent(cache, _ -> new AtomicLong());
	}

	private void onChange(final Iterable<CacheEntryEvent<?, ?>> events) {
		if (events.iterator().hasNext()) {
			increment(cache);
		}
	}

	@Override
	public void onRemoved(final Iterable<CacheEntryEvent<?, ?>> events) {
		onChange(events);
	}

	@Override
	public void onUpdated(final Iterable<CacheEntryEvent<?, ?>> events) {
		onChange(events);
	}

	@Override
	public void onExpired(final Iterable<CacheEntryEvent<?, ?>> events) {
		onChange(events);
	}
}
