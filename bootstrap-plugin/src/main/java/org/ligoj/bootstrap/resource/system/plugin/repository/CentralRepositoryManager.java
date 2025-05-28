/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.HttpMethod;
import org.ligoj.bootstrap.core.curl.CurlProcessor;
import org.ligoj.bootstrap.core.curl.CurlRequest;
import org.springframework.stereotype.Component;

import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maven central repository.
 */
@Component
public class CentralRepositoryManager extends AbstractRemoteRepositoryManager {

	private static final String DEFAULT_ARTIFACT_URL = "https://repo.maven.apache.org/maven2/";
	private static final String DEFAULT_GROUP_ID = "org.ligoj.plugin";

	// See https://central.sonatype.org/search/rest-api-guide/
	private static final String DEFAULT_SEARCH_URL = "https://search.maven.org/solrsearch/select?wt=json&rows=100&q=g:";
	private static final String DEFAULT_SEARCH_URL_V2 = "https://central.sonatype.com/api/internal/browse/components";


	@Override
	public String getId() {
		return "central";
	}

	@CacheResult(cacheName = "plugins-last-version-central-v2")
	public Map<String, Artifact> getLastPluginVersionsV2() throws IOException {
		try (var processor = new CurlProcessor(getSearchProxyHost(), getSearchProxyPort())) {
			var page = 0;
			final var groupId = getGroupId(DEFAULT_GROUP_ID);
			final var curlRequest = new CurlRequest(HttpMethod.POST, getSearchUrl(DEFAULT_SEARCH_URL),
					"{\"page\":"+page+",\"size\":20,\"searchTerm\":\"\",\"sortField\":\"publishedDate\",\"sortDirection\":\"desc\",\"filter\":[\"namespace:"
							+ groupId + "\"]}",
					"content-type:application/json");
			curlRequest.setSaveResponse(true);
			processor.process(curlRequest);
			final var searchResult = Objects.toString(curlRequest.getResponse(), "{\"components\": [], \"pageCount\":0}");
			// Extract artifacts
			final var jsonMapper = new ObjectMapper();
			return Arrays.stream(jsonMapper.treeToValue(jsonMapper.readTree(searchResult).at("/components"), CentralSearchResultV2[].class))
					.collect(Collectors.toMap(CentralSearchResultV2::getArtifact, ArtifactVo::new));
		}
	}

	@Override
	@CacheResult(cacheName = "plugins-last-version-central")
	public Map<String, Artifact> getLastPluginVersions() throws IOException {
		try (var processor = new CurlProcessor(getSearchProxyHost(), getSearchProxyPort())) {
			final var searchResult = Objects.toString(processor.get(getSearchUrl(DEFAULT_SEARCH_URL + getGroupId(DEFAULT_GROUP_ID))),
					"{\"response\":{\"docs\":[]}}}");
			// Extract artifacts
			final var jsonMapper = new ObjectMapper();
			return Arrays.stream(jsonMapper.treeToValue(jsonMapper.readTree(searchResult).at("/response/docs"), CentralSearchResult[].class))
					.collect(Collectors.toMap(CentralSearchResult::getArtifact, ArtifactVo::new));
		}
	}

	@Override
	public InputStream getArtifactInputStream(String groupId, String artifact, String version, final String classifier) throws IOException {
		return getArtifactInputStream(groupId, artifact, version, DEFAULT_ARTIFACT_URL, classifier);
	}

	@Override
	@CacheRemoveAll(cacheName = "plugins-last-version-central")
	public void invalidateLastPluginVersions() {
		// Nothing to do
	}

}
