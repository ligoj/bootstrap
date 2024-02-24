/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.plugin.javadoc;

import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.openapi.OpenApiCustomizer;
import org.ligoj.bootstrap.dao.system.SystemPluginRepository;
import org.ligoj.bootstrap.model.system.SystemPlugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;

public class LigojOpenApiCustomizer extends OpenApiCustomizer {

	private final SystemPluginRepository repository;

	public LigojOpenApiCustomizer(List<URL> javadocUrls, SystemPluginRepository repository) {
		this.repository = repository;
		setDynamicBasePath(false);
		setJavadocProvider(new DocumentationProvider(new URLClassLoader(javadocUrls.toArray(new URL[0]))));
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

	protected void fillSummaryAndDescription(final String fullDoc, final Consumer<String> setSummary, final Consumer<String> setDescription) {
		if (fullDoc != null) {
			// Split the documentation into 'summary' and 'description'
			final var eos = fullDoc.indexOf('.');
			if (eos == -1 || eos == fullDoc.length() - 1) {
				setSummary.accept(removeUselessChars(fullDoc));
			} else {
				setSummary.accept(fullDoc.substring(0, eos));
				if (setDescription != null) {
					setDescription.accept(removeUselessChars(fullDoc.substring(eos + 1)));
				}
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
			var artifact = packageToPlugin.computeIfAbsent(cri.getResourceClass().getPackageName(), p -> plugins.stream().filter(plugin1 -> p.startsWith(plugin1.getBasePackage())).min(Comparator.comparing(SystemPlugin::getBasePackage)).orElse(new SystemPlugin())).getArtifact();
			if (artifact == null) {
				artifact = StringUtils.split(pathKey, '/')[0];
			}

			final var tagOperation = artifact;
			pathItem.readOperationsMap().forEach((method, operation) -> {
				operation.setTags(Collections.singletonList(tagOperation));
				var key = Pair.of(method.name(), pathKey);
				if (methods.containsKey(key)) {
					var ori = methods.get(key);
					tags.computeIfAbsent(tagOperation, t -> javadocProvider.getClassDoc(cri));
					fillSummaryAndDescription(javadocProvider.getMethodDoc(ori), operation);

					if (operation.getParameters() == null) {
						var parameters = new ArrayList<Parameter>();
						addParameters(parameters);
						operation.setParameters(parameters);
					}

					for (var i = 0; i < operation.getParameters().size(); i++) {
						if (StringUtils.isBlank(operation.getParameters().get(i).getDescription())) {
							operation.getParameters().get(i).setDescription(extractJavadoc(operation, ori, i));
						}
					}
					addParameters(operation.getParameters());
					customizeResponses(operation, ori);
				}
			});
		});
		oas.setTags(tags.keySet().stream().sorted().map(t -> {
			var tag = new Tag().name(t);
			fillSummaryAndDescription(tags.get(t), tag::description, null);
			return tag;
		}).toList());
	}

	/**
	 * Remove useless chars from documentation lines.
	 */
	private String removeUselessChars(String doc) {
		return StringUtils.removeEnd(doc, ".");
	}
}
