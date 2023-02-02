/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.cache.impl.CacheProxy;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.internal.cluster.ClusterService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache resource.
 */
@Path("/system/cache")
@Service
@Transactional
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
public class CacheResource implements ApplicationListener<ContextClosedEvent> {

	@Autowired
	protected CacheManager cacheManager;

	@Setter
	@Value("${hazelcast.statistics.enable:false}")
	private boolean statisticsEnabled = false;

	/**
	 * Return the installed caches.
	 *
	 * @return the installed caches
	 */
	@GET
	public List<CacheStatistics> getCaches() {
		return cacheManager.getCacheNames().stream().map(this::getCache).toList();
	}

	/**
	 * Return the information of given cache.
	 *
	 * @param name the cache's name to display.
	 *
	 * @return the cache's configuration.
	 */
	@GET
	@Path("{name:[\\w\\-]+}")
	public CacheStatistics getCache(@PathParam("name") final String name) {
		final var result = new CacheStatistics();
		final var cache = getCacheNative(name);
		final var node = newCacheNode(cache.getNodeEngine().getLocalMember());
		node.setCluster(newCacheCluster(cache.getNodeEngine().getClusterService()));
		result.setId(name);
		result.setNode(node);
		setStatistics(result, cache.getLocalCacheStatistics());
		return result;
	}

	@SuppressWarnings("unchecked")
	private CacheProxy<Object, Object> getCacheNative(final String name) {
		return (CacheProxy<Object, Object>) getCacheExpected(name).getNativeCache();
	}

	private Cache getCacheExpected(final String name) {
		return Optional.ofNullable(cacheManager.getCache(name)).orElseThrow(() -> new EntityNotFoundException(name));
	}

	/**
	 * Update the statistics.
	 *
	 * @param result     The target result.
	 * @param statistics The source statistics.
	 */
	protected void setStatistics(final CacheStatistics result, final com.hazelcast.cache.CacheStatistics statistics) {
		result.setSize(statistics.getOwnedEntryCount());
		// Only when statistics are enabled
		if (statisticsEnabled) {
			result.setMissPercentage(statistics.getCacheMissPercentage());
			result.setMissCount(statistics.getCacheMisses());
			result.setHitPercentage(statistics.getCacheHitPercentage());
			result.setHitCount(statistics.getCacheHits());
			result.setAverageGetTime(statistics.getAverageGetTime());
		}
	}

	private CacheNode newCacheNode(Member member) {
		final var node = new CacheNode();
		node.setAddress(Objects.toString(member.getAddress()));
		node.setId(member.getUuid().toString());
		node.setVersion(Objects.toString(member.getVersion()));
		return node;
	}

	private CacheCluster newCacheCluster(ClusterService clusterService) {
		final var cluster = new CacheCluster();
		cluster.setId(clusterService.getClusterId().toString());
		cluster.setState(clusterService.getClusterState().toString());
		cluster.setMembers(clusterService.getMembers().stream().map(this::newCacheNode).toList());
		return cluster;
	}

	/**
	 * Invalidate a specific cache.
	 *
	 * @param name the cache's name to invalidate.
	 */
	@POST
	@DELETE
	@Path("{name:[\\w\\-]+}")
	public void invalidate(@PathParam("name") final String name) {
		getCacheExpected(name).clear();
	}

	/**
	 * Enable all cache statistics
	 */
	@POST
	@Path("statistics/enable")
	public void enableStatistics() {
		changeStatistics(true);
	}

	/**
	 * Disable all cache statistics
	 */
	@POST
	@Path("statistics/disable")
	public void disableStatistics() {
		changeStatistics(false);
	}

	private void changeStatistics(final boolean enabled) {
		getCacheNative("authorizations").getService().getCacheConfigs().forEach(c -> c.setStatisticsEnabled(enabled));
		statisticsEnabled = enabled;
	}

	/**
	 * Invalidate all caches.
	 */
	@DELETE
	public void invalidate() {
		cacheManager.getCacheNames().stream().map(cacheManager::getCache).forEach(Cache::clear);
	}

	@Override
	public void onApplicationEvent(final ContextClosedEvent event) {
		log.info("Stopping context detected, shutdown the Hazelcast instance");
		final var manager = (HazelcastCacheManager) ((JCacheCacheManager) cacheManager).getCacheManager();
		try {
			manager.getHazelcastInstance().getLifecycleService().terminate();
		} catch (HazelcastInstanceNotActiveException he) {
			log.info("Hazelcast node was already terminated: {}", he.getMessage());
		}
	}
}
