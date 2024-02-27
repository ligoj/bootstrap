/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.ParameterType;
import org.apache.cxf.jaxrs.openapi.OpenApiCustomizer;
import org.ligoj.bootstrap.dao.system.SystemPluginRepository;
import org.ligoj.bootstrap.model.system.SystemPlugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;

/**
 * OpenAPI customizer with JavaDoc contribution.
 */
public class LigojOpenApiCustomizer extends OpenApiCustomizer {

	/**
	 * Empty plugin for default management.
	 */
	private static final SystemPlugin DEFAULT_PLUGIN = new SystemPlugin();

	private final SystemPluginRepository repository;

	/**
	 * Construction from JavaDoc URLs and plugin registry.
	 *
	 * @param javadocUrls Source JavaDoc URLs.
	 * @param repository  Plugin registry.
	 */
	public LigojOpenApiCustomizer(List<URL> javadocUrls, SystemPluginRepository repository) {
		this.repository = repository;
		setDynamicBasePath(false);
		setJavadocProvider(new JavadocDocumentationProvider(new URLClassLoader(javadocUrls.toArray(new URL[0]))));
	}

	@Override
	public OpenAPIConfiguration customize(final OpenAPIConfiguration configuration) {
		super.customize(configuration);
		configuration.getOpenAPI().setServers(List.of(new Server().url("./").description("REST API Server")));
		return configuration;
	}

	@Override
	protected String getNormalizedPath(String classResourcePath, String operationResourcePath) {
		final var normalizedPath = new StringBuilder();
		final var segments = (classResourcePath + operationResourcePath).split("/");
		for (var segment : segments) {
			if (!StringUtils.isEmpty(segment)) {
				// Remove parameterized notations from the key
				normalizedPath.append('/').append(segment.replaceAll(":.*}", "}"));
			}
		}
		return StringUtils.EMPTY.contentEquals(normalizedPath) ? "/" : normalizedPath.toString();
	}

	private void fillSummaryAndDescription(final String fullDoc, final Operation operation) {
		fillSummaryAndDescription(fullDoc, operation::setSummary, operation::setDescription);
	}

	private void fillSummaryAndDescription(final String fullDoc, final Consumer<String> setSummary, final Consumer<String> setDescription) {
		if (fullDoc != null) {
			// Split the documentation into 'summary' and 'description'
			setSummary.accept(JavadocDocumentationProvider.normalize(StringUtils.substringBefore(fullDoc, ".")));
			if (setDescription != null) {
				setDescription.accept(JavadocDocumentationProvider.normalize(StringUtils.substringAfter(fullDoc, ".")));
			}
		}
	}

	@Override
	public void customize(final OpenAPI oas) {
		final var operations = new HashMap<String, ClassResourceInfo>();
		final var methods = new HashMap<Pair<String, String>, OperationResourceInfo>();
		cris.forEach(cri -> cri.getMethodDispatcher().getOperationResourceInfos().forEach(ori -> {
			var normalizedPath = getNormalizedPath(cri.getURITemplate().getValue(), ori.getURITemplate().getValue());
			operations.put(normalizedPath, cri);
			methods.put(Pair.of(ori.getHttpMethod(), normalizedPath), ori);
		}));

		// Check Javadoc completeness is necessary
		if (oas.getExtensions() != null) {
			return;
		}
		// Reorder the OpenAPI path by natural language
		final var sortedPaths = new Paths();
		oas.setExtensions(Map.of("sort", new PathItem()));
		oas.getPaths().entrySet().stream().sorted(Comparator.comparing(path -> path.getKey().replace('{', '_'))).forEach(entry -> sortedPaths.addPathItem(entry.getKey(), entry.getValue()));
		oas.setPaths(sortedPaths);

		// Complete doc and tags
		final var plugins = repository.findAll();
		final var packageToPlugin = new HashMap<String, SystemPlugin>();
		final var tags = new HashMap<String, String>();
		oas.getPaths().forEach((pathKey, pathItem) -> {
			var cri = operations.get(pathKey);
			if (cri == null) {
				return;
			}
			var artifact = packageToPlugin.computeIfAbsent(cri.getResourceClass().getPackageName(), p -> plugins.stream()
					.filter(plugin1 -> p.startsWith(plugin1.getBasePackage()))
					.min(Comparator.comparing(SystemPlugin::getBasePackage))
					.orElse(DEFAULT_PLUGIN)).getArtifact();
			if (artifact == null) {
				artifact = StringUtils.split(pathKey, '/')[0];
			}

			final var tagOperation = artifact;
			pathItem.readOperationsMap().forEach((method, operation) -> {
				operation.setTags(Collections.singletonList(tagOperation));
				var key = Pair.of(method.name(), pathKey);
				if (methods.containsKey(key)) {
					final var ori = methods.get(key);
					tags.computeIfAbsent(tagOperation, t -> javadocProvider.getClassDoc(cri));
					fillSummaryAndDescription(javadocProvider.getMethodDoc(ori), operation);
					for (var i = 0; i < CollectionUtils.emptyIfNull(operation.getParameters()).size(); i++) {
						operation.getParameters().get(i).setDescription(JavadocDocumentationProvider.normalize(extractJavadoc(operation, ori, i)));
					}
					if (operation.getRequestBody() != null) {
						for (var i = 0; i < ori.getParameters().size(); i++) {
							final var parameter = ori.getParameters().get(i);
							if (parameter.getType() == ParameterType.REQUEST_BODY) {
								operation.getRequestBody().setDescription(JavadocDocumentationProvider.normalize(javadocProvider.getMethodParameterDoc(ori, i)));
							}
						}
					}

					customizeResponses(operation, ori);
				}
			});
		});
		oas.setTags(tags.keySet().stream().sorted().map(t -> {
			final var tag = new Tag().name(t);
			fillSummaryAndDescription(tags.get(t), tag::description, null);
			return tag;
		}).toList());
	}
}