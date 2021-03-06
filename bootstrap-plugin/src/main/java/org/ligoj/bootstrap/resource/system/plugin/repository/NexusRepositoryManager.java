/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;

import org.apache.commons.lang3.StringUtils;
import org.ligoj.bootstrap.core.curl.CurlProcessor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Nexus repository manager.
 */
@Component
public class NexusRepositoryManager extends AbstractRemoteRepositoryManager {

	private static final String DEFAULT_ARTIFACT_URL = "https://oss.sonatype.org/service/local/repositories/releases/content/";
	private static final String DEFAULT_GROUP_ID = "org.ligoj.plugin";
	private static final String DEFAULT_SEARCH_URL = "https://oss.sonatype.org/service/local/lucene/search?collapseresults=true&repositoryId=releases&p=jar&c=sources&g=";

	@Override
	@CacheResult(cacheName = "plugins-last-version-nexus")
	public Map<String, Artifact> getLastPluginVersions() throws IOException {
		try (var processor = new CurlProcessor()) {
			final var searchResult = StringUtils.defaultString(
					processor.get(getSearchUrl(DEFAULT_SEARCH_URL + getGroupId(DEFAULT_GROUP_ID)), "Accept:application/json"), "{\"data\":[]}");
			// Extract artifacts
			final var jsonMapper = new ObjectMapper();
			return Arrays.stream(jsonMapper.treeToValue(jsonMapper.readTree(searchResult).at("/data"), NexusSearchResult[].class))
					.collect(Collectors.toMap(NexusSearchResult::getArtifact, ArtifactVo::new, (a1, a2) -> a1));
		}
	}

	@Override
	public String getId() {
		return "nexus";
	}

	@Override
	public InputStream getArtifactInputStream(String artifact, String version) throws IOException {
		return getArtifactInputStream(artifact, version, DEFAULT_ARTIFACT_URL + getGroupId(DEFAULT_GROUP_ID).replace('.', '/'));
	}

	@Override
	@CacheRemoveAll(cacheName = "plugins-last-version-nexus")
	public void invalidateLastPluginVersions() {
		// Nothing to do
	}

}
