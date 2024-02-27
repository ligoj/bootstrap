/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.cxf.jaxrs.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ligoj.bootstrap.core.plugin.PluginsClassLoader;
import org.ligoj.bootstrap.dao.system.SystemPluginRepository;
import org.ligoj.bootstrap.model.system.SystemPlugin;
import org.ligoj.bootstrap.model.system.SystemUser;
import org.ligoj.bootstrap.resource.system.plugin.SampleTool1;
import org.ligoj.bootstrap.resource.system.plugin.SampleTool2;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test class of {@link LigojOpenApiCustomizer}
 */
class LigojOpenApiCustomizerTest extends org.ligoj.bootstrap.AbstractServerTest {

	private static final String USER_HOME_DIRECTORY = "target/test-classes/home-test";

	@Test
	void customize() throws IOException {
		final var jarPath = Paths.get(USER_HOME_DIRECTORY, PluginsClassLoader.HOME_DIR_FOLDER, PluginsClassLoader.PLUGINS_DIR).resolve("plugin-javadoc.jar");
		final var pluginFiles = Map.of("plugin-javadoc", jarPath.toString());
		final var versionFileToPath = Map.of(jarPath.toString(), jarPath);
		final var javadocUrls = new ArrayList<URL>();
		for (final var path : pluginFiles.values()) {
			final var javadocPath = versionFileToPath.get(path);
			javadocUrls.add(javadocPath.toUri().toURL());
		}

		final var repository = Mockito.mock(SystemPluginRepository.class);
		final var plugin1 = new SystemPlugin();
		plugin1.setBasePackage(SampleTool2.class.getPackageName());
		plugin1.setArtifact("tool1");
		final var plugin2 = new SystemPlugin();
		plugin2.setBasePackage("org.ligoj.bootstrap.resource");
		plugin2.setArtifact("tool2");
		final var plugin3 = new SystemPlugin();
		plugin3.setBasePackage("foo.bar");
		plugin3.setArtifact("tool3");
		Mockito.doReturn(List.of(plugin1, plugin2, plugin3)).when(repository).findAll();

		final var configuration = new SwaggerConfiguration();
		configuration.setOpenAPI(new OpenAPI());
		final var customizer = new LigojOpenApiCustomizer(javadocUrls, repository);
		customizer.customize(configuration);
		final var cri1 = new ClassResourceInfo(SampleTool1.class);
		cri1.setMethodDispatcher(new MethodDispatcher());
		cri1.setURITemplate(new URITemplate("mock/sample1"));
		final var method = MethodUtils.getMatchingMethod(SampleTool1.class, "test1", String.class, SystemUser.class);
		final var parameter1 = new Parameter(ParameterType.QUERY, 0,"param1");
		final var parameter2 = new Parameter(ParameterType.REQUEST_BODY, 1,"user");
		final var ori = new OperationResourceInfo(method, cri1, new URITemplate("/{param1:regEx}"), "POST", "application/json", "application/json", List.of(parameter1, parameter2), true);
		cri1.getMethodDispatcher().bind(ori, method);
		final var cri2 = new ClassResourceInfo(SampleTool2.class);
		cri2.setMethodDispatcher(new MethodDispatcher());
		cri2.setURITemplate(new URITemplate("mock/sample2"));
		final var method2 = MethodUtils.getMatchingMethod(SampleTool2.class, "test");
		final var ori2 = new OperationResourceInfo(method2, cri2, new URITemplate(""), "GET", "application/json", "application/json", Collections.emptyList(), true);
		cri2.getMethodDispatcher().bind(ori2, method2);

		final var cri3 = new ClassResourceInfo(SystemPlugin.class);
		cri3.setMethodDispatcher(new MethodDispatcher());
		cri3.setURITemplate(new URITemplate("mock/sample3"));
		final var method3 = MethodUtils.getMatchingMethod(SystemPlugin.class, "getVersion");
		final var ori3 = new OperationResourceInfo(method3, cri3, new URITemplate(""), "GET", "application/json", "application/json", Collections.emptyList(), true);
		cri3.getMethodDispatcher().bind(ori3, method3);

		final var cri4 = new ClassResourceInfo(SampleTool2.class);
		cri4.setMethodDispatcher(new MethodDispatcher());

		final var cris = List.of(cri1, cri2, cri3, cri4);
		customizer.setClassResourceInfos(cris);
		final var oas = new OpenAPI();
		final var sortedPaths = new io.swagger.v3.oas.models.Paths();
		final var pathItem1 = new PathItem();
		final var oasOperation = new Operation();
		final var oasParameter = new io.swagger.v3.oas.models.parameters.Parameter().name("param1");
		oasOperation.setParameters(List.of(oasParameter));
		oasOperation.setRequestBody(new RequestBody());
		pathItem1.operation(PathItem.HttpMethod.POST, oasOperation);
		sortedPaths.put("/mock/sample1/{param1}", pathItem1);

		final var pathItem2 = new PathItem();
		final var oasOperation2 = new Operation();
		pathItem2.operation(PathItem.HttpMethod.GET, oasOperation2);
		sortedPaths.put("/mock/sample2", pathItem2);
		sortedPaths.put("/mock/sample3", new PathItem());
		oas.setPaths(sortedPaths);

		customizer.customize(oas);

		// Repeat with cache
		customizer.customize(oas);

		Assertions.assertEquals("Method doc", oasOperation.getSummary());
		Assertions.assertEquals("Details", oasOperation.getDescription());
		Assertions.assertEquals("User doc. Details", oasOperation.getRequestBody().getDescription());
		Assertions.assertEquals("Param1 doc", oasParameter.getDescription());
	}

}