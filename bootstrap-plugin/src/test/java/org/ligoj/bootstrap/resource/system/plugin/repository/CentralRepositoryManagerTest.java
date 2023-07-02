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
 * Test class of {@link CentralRepositoryManager}
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/application-context-test.xml")
@Rollback
@Transactional
class CentralRepositoryManagerTest extends org.ligoj.bootstrap.AbstractServerTest {

	@Autowired
	private CentralRepositoryManager resource;

	@BeforeEach
	void prepareData() throws IOException {
		persistEntities("csv-test", new Class[] { SystemConfiguration.class }, StandardCharsets.UTF_8);
	}

	@Test
	void invalidateLastPluginVersions() throws IOException {
		httpServer.stubFor(get(urlEqualTo("/solrsearch/select?wt=json&rows=100&q=org.ligoj.plugin"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)
						.withBody(IOUtils.toString(
								new ClassPathResource("mock-server/maven-repo/search.json").getInputStream(),
								StandardCharsets.UTF_8))));
		httpServer.start();
		resource.invalidateLastPluginVersions();
		final var versions = resource.getLastPluginVersions();
		Assertions.assertEquals(versions.keySet(), resource.getLastPluginVersions().keySet());
		resource.invalidateLastPluginVersions();
		Assertions.assertEquals(versions.keySet(), resource.getLastPluginVersions().keySet());
	}
}
