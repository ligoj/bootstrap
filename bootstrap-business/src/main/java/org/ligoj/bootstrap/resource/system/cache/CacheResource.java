/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.hazelcast.cache.impl.CacheProxy;
import com.hazelcast.core.Member;
import com.hazelcast.internal.cluster.ClusterService;
import com.hazelcast.monitor.LocalMapStats;

/**
 * Cache resource.
 */
@Path("/system/cache")
@Service
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class CacheResource {

	@Autowired
	private CacheManager cacheManager;

	/**
	 * Return the installed caches.
	 * 
	 * @return the installed caches
	 */
	@GET
	public List<CacheStatistics> getCaches() {
		final List<CacheStatistics> result = new ArrayList<>();
		for (final String cache : cacheManager.getCacheNames()) {
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
		@SuppressWarnings("unchecked")
		final CacheProxy<Object, Object> cache = (CacheProxy<Object, Object>) cacheManager.getCache(name).getNativeCache();
		result.setId(name);

		final CacheNode node = newCacheNode(cache.getNodeEngine().getLocalMember());
		node.setCluster(newCacheCluster(cache.getNodeEngine().getClusterService()));

		final LocalMapStats stats = cache.getNodeEngine().getHazelcastInstance().getMap(name).getLocalMapStats();
		stats.toJson();
		
		final com.hazelcast.cache.CacheStatistics statistics = cache.getLocalCacheStatistics();
		result.setNode(node);
		result.setSize(statistics.getOwnedEntryCount());
		result.setMissPercentage(statistics.getCacheMissPercentage());
		result.setHitCount(statistics.getCacheHits());
		result.setMissCount(statistics.getCacheMisses());
		result.setAverageGetTime(statistics.getAverageGetTime());
		return result;
	}

	private CacheNode newCacheNode(Member member) {
		final CacheNode node = new CacheNode();
		node.setAddress(Objects.toString(member.getAddress()));
		node.setId(member.getUuid());
		node.setVersion(Objects.toString(member.getVersion()));
		return node;
	}

	private CacheCluster newCacheCluster(ClusterService clusterService) {
		final CacheCluster cluser = new CacheCluster();
		cluser.setId(clusterService.getClusterId());
		cluser.setState(clusterService.getClusterState().toString());
		cluser.setMembers(clusterService.getMembers().stream().map(this::newCacheNode).collect(Collectors.toList()));
		return cluser;
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
		cacheManager.getCache(name).clear();
	}
}
