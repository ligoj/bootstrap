package org.ligoj.bootstrap.resource.system.cache;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.StatisticsGateway;

/**
 * Cache resource.
 */
@Path("/system/cache")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class CacheResource {

	/**
	 * Return the installed caches.
	 * 
	 * @return the installed caches
	 */
	@GET
	public List<CacheStatistics> getCaches() {
		final List<CacheStatistics> result = new ArrayList<>();
		for (final String cache : CacheManager.getInstance().getCacheNames()) {
			result.add(getCache(cache));
		}
		return result;
	}

	/**
	 * Return the information of given cache.
	 * 
	 * @param name
	 *            the cache's name to display.
	 * 
	 * @return the cache's configuration.
	 */
	@GET
	@Path("{name:[\\w\\-]+}")
	public CacheStatistics getCache(@PathParam("name") final String name) {
		final CacheStatistics result = new CacheStatistics();
		final Cache cache = CacheManager.getInstance().getCache(name);
		final StatisticsGateway statistics = cache.getStatistics();
		result.setId(cache.getGuid());
		result.setSize(cache.getKeys().size());
		result.setName(cache.getName());
		result.setHitCount(statistics.cacheHitCount());
		result.setMissCount(statistics.cacheMissCount());
		result.setBytes(statistics.getLocalHeapSizeInBytes());
		result.setOffHeapBytes(statistics.getLocalOffHeapSizeInBytes());
		return result;
	}

	/**
	 * Invalidate a specific cache.
	 * 
	 * @param name
	 *            the cache's anme to invalidate.
	 */
	@POST
	@Path("{name:[\\w\\-]+}")
	public void invalidate(@PathParam("name") final String name) {
		CacheManager.getInstance().getCache(name).removeAll();
	}
}
