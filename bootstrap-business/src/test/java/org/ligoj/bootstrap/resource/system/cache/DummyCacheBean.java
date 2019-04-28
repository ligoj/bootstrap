/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;

import org.springframework.stereotype.Component;

/**
 * Test bean for JSR-107 tests
 */
@Component
public class DummyCacheBean {

	public static int hit = 0;

	/**
	 * Cache efficiency test.
	 * 
	 * @return the last #hit counter known by the cache.
	 */
	@CacheResult(cacheName = "test-cache")
	public int getHit(@CacheKey final String entry) {
		return ++hit;
	}

	/**
	 * Cache efficiency test.
	 */
	@CachePut(cacheName = "test-cache")
	void updateHit(@CacheKey final String entry, @CacheValue int value) {
		hit = value;
	}

	/**
	 * Cache efficiency test for "test-cache-1"
	 * 
	 * @return the last #hit counter known by the cache.
	 */
	@CacheResult(cacheName = "test-cache-1")
	public int getHit1(@CacheKey final String entry) {
		return ++hit;
	}

	/**
	 * Cache efficiency test for "test-cache-1"
	 */
	@CachePut(cacheName = "test-cache-1")
	void updateHit1(@CacheKey final String entry, @CacheValue int value) {
		hit = value;
	}

	/**
	 * Cache efficiency test for "test-cache-2"
	 * 
	 * @return the last #hit counter known by the cache.
	 */
	@CacheResult(cacheName = "test-cache-2")
	public int getHit2(@CacheKey final String entry) {
		return ++hit;
	}

	/**
	 * Cache efficiency test for "test-cache-2"
	 */
	@CachePut(cacheName = "test-cache-2")
	void updateHit2(@CacheKey final String entry, @CacheValue int value) {
		hit = value;
	}



}
