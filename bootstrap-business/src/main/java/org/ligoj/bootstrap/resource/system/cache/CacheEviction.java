/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

/**
 * Cache eviction helper for the data derived from the database, such as the security caches. The eviction is performed
 * immediately, and once again after the commit of the current transaction, if any: a concurrent request reading the
 * previous state between these two moments would otherwise cache it again, until the expiration of the entry.
 */
public final class CacheEviction {

	private CacheEviction() {
		// Utility class
	}

	/**
	 * Clear the given caches.
	 *
	 * @param manager The cache manager.
	 * @param names   The cache names.
	 */
	public static void clear(final CacheManager manager, final String... names) {
		nowAndAfterCommit(() -> {
			for (final var name : names) {
				Optional.ofNullable(manager.getCache(name)).ifPresent(Cache::clear);
			}
		});
	}

	/**
	 * Evict an entry of the given cache.
	 *
	 * @param manager The cache manager.
	 * @param name    The cache name.
	 * @param key     The entry key.
	 */
	public static void evict(final CacheManager manager, final String name, final Object key) {
		nowAndAfterCommit(() -> Optional.ofNullable(manager.getCache(name)).ifPresent(c -> c.evict(key)));
	}

	private static void nowAndAfterCommit(final Runnable eviction) {
		eviction.run();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					eviction.run();
				}
			});
		}
	}
}
