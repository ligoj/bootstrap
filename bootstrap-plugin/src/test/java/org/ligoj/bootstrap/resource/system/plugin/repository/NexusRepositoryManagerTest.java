/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.repository;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ligoj.bootstrap.AbstractServerTest;
import org.ligoj.bootstrap.model.system.SystemConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Test class of {@link NexusRepositoryManager}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
class NexusRepositoryManagerTest extends AbstractServerTest {

	@Autowired
	private NexusRepositoryManager resource;

	@BeforeEach
	void prepareData() throws IOException {
		persistEntities("csv-test", new Class[] { SystemConfiguration.class }, StandardCharsets.UTF_8.name());
	}

	@Test
	void invalidateLastPluginVersions() throws IOException {
		httpServer.stubFor(get(urlEqualTo(
				"/service/local/lucene/search?g=org.ligoj.plugin&collapseresults=true&repositoryId=releases&p=jar&c=sources"))
						.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
								.withBody(IOUtils.toString(
										new ClassPathResource("mock-server/nexus-repo/search.json").getInputStream(),
										StandardCharsets.UTF_8))));
		httpServer.start();
		final var versions = resource.getLastPluginVersions();
		Assertions.assertEquals(versions.keySet(), resource.getLastPluginVersions().keySet());
		Assertions.assertEquals(2, versions.size());
		Assertions.assertEquals("0.0.1", versions.get("plugin-sample").getVersion());
		resource.invalidateLastPluginVersions();
		Assertions.assertEquals(versions.keySet(), resource.getLastPluginVersions().keySet());
	}
}
