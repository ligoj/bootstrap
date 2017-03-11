package org.ligoj.bootstrap.resource.system.cache;

import javax.cache.annotation.CacheResult;

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
	public int getHit() {
		return ++hit;
	}

}
