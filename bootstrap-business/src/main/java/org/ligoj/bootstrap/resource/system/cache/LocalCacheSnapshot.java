/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.util.function.Supplier;

/**
 * Local snapshot of a cluster cached value: the value is kept in this node memory while the {@link CacheGeneration} of
 * its cache is unchanged, avoiding a cluster access and a deserialization on each read. The cache must be watched with
 * {@link CacheGeneration#watch(com.hazelcast.config.CacheConfig)}.
 *
 * @param <T> The value type.
 */
public class LocalCacheSnapshot<T> {

	private record Holder<T>(long generation, T value) {
	}

	private final String cache;

	private volatile Holder<T> holder;

	/**
	 * Snapshot of the given cache.
	 *
	 * @param cache The cache name.
	 */
	public LocalCacheSnapshot(final String cache) {
		this.cache = cache;
	}

	/**
	 * Return the snapshot value, loaded again when the cache has changed since the last load.
	 *
	 * @param loader The value loader, usually the cached method.
	 * @return The value.
	 */
	public T get(final Supplier<T> loader) {
		// Generation read before the load: a change during the load causes a new load on the next call
		final var generation = CacheGeneration.get(cache);
		final var current = holder;
		if (current != null && current.generation() == generation) {
			return current.value();
		}
		final var value = loader.get();
		holder = new Holder<>(generation, value);
		return value;
	}
}
