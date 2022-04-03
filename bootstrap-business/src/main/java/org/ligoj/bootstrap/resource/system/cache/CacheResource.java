/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import com.hazelcast.cache.HazelcastCacheManager;
import com.hazelcast.cache.impl.CacheProxy;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.Member;
import com.hazelcast.internal.cluster.ClusterService;

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

	/**
	 * Return the installed caches.
	 *
	 * @return the installed caches
	 */
	@GET
	public List<CacheStatistics> getCaches() {
		final List<CacheStatistics> result = new ArrayList<>();
		for (final var cache : cacheManager.getCacheNames()) {
			result.add(getCache(cache));
		}
		return result;
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
		@SuppressWarnings("unchecked")
		final var cache = (CacheProxy<Object, Object>) getCacheExpected(name).getNativeCache();
		final var node = newCacheNode(cache.getNodeEngine().getLocalMember());
		node.setCluster(newCacheCluster(cache.getNodeEngine().getClusterService()));
		result.setId(name);
		result.setNode(node);
		setStatistics(result, cache.getLocalCacheStatistics());
		return result;
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
		if (isStatisticEnabled()) {
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
		node.setId(member.getUuid());
		node.setVersion(Objects.toString(member.getVersion()));
		return node;
	}

	private CacheCluster newCacheCluster(ClusterService clusterService) {
		final var cluster = new CacheCluster();
		cluster.setId(clusterService.getClusterId());
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
	 * Invalidate all caches.
	 */
	@DELETE
	public void invalidate() {
		cacheManager.getCacheNames().stream().map(cacheManager::getCache).forEach(Cache::clear);
	}

	/**
	 * Indicates the statistics are enabled or nor.
	 *
	 * @return <code>true</code> when statistics are enabled. Based on <code>java.security.policy</code>.
	 */
	public static boolean isStatisticEnabled() {
		// When a policy is defined, assume JMX is enabled
		return System.getProperty("java.security.policy") != null;
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
