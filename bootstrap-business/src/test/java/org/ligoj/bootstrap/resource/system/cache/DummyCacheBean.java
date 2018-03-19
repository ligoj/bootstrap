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
	 * 
	 * @return the last #hit counter known by the cache.
	 */
	@CachePut(cacheName = "test-cache")
	public void updateHit(@CacheKey final String entry, @CacheValue int value) {
		hit = value;
	}

}
