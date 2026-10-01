/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.core.dao.AbstractBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Test class of {@link CacheEviction}.
 */
@ExtendWith(SpringExtension.class)
class CacheEvictionTest extends AbstractBootTest {

	/**
	 * Simulate the commit of the current transaction for the registered synchronizations.
	 */
	static void afterCommit() {
		TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
	}

	@Test
	void clear() {
		final var cache = cacheManager.getCache("user-details");
		cache.put("user", "details");
		CacheEviction.clear(cacheManager, "user-details", "not-exists");
		Assertions.assertNull(cache.get("user"));

		// Data cached again by a concurrent request before the commit, still reading the old state
		cache.put("user", "stale");
		afterCommit();
		Assertions.assertNull(cache.get("user"));
	}

	@Test
	void evict() {
		final var cache = cacheManager.getCache("user-details");
		cache.put("user", "details");
		cache.put("other", "details");
		CacheEviction.evict(cacheManager, "user-details", "user");
		Assertions.assertNull(cache.get("user"));
		cache.put("user", "stale");
		afterCommit();
		Assertions.assertNull(cache.get("user"));
		Assertions.assertNotNull(cache.get("other"));
	}
}
