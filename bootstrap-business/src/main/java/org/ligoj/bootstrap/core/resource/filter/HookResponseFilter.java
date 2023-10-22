/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.apache.cxf.jaxrs.impl.AbstractPropertiesImpl;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.dao.system.SystemHookRepository;
import org.ligoj.bootstrap.model.system.HookMatch;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.hook.HookProcessRunnable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.cache.annotation.CacheResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A response filter able to transform Null results to 404 HTTP response when
 */
@Provider
public class HookResponseFilter extends AbstractMapper implements ContainerResponseFilter {

	@Autowired
	protected SystemHookRepository repository;

	@Autowired
	private HookResponseFilter self = this;

	@Autowired
	private ObjectMapperTrim objectMapper;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	void execute(final HookProcessRunnable runnable) {
		executor.execute(runnable);
	}

	@CacheResult(cacheName = "hooks")
	public Map<Pattern, List<SystemHook>> findAll() {
		final var patterns = repository.findAll().stream().peek(
				h -> {
					try {
						h.setMatchObject(new ObjectMapper().readValue(h.getMatch(), HookMatch.class));
					} catch (final IOException ioe) {
						// Ignore
					}
				}
		).collect(Collectors.groupingBy(h -> h.getMatchObject().getPath()));
		return patterns.entrySet().stream().collect(Collectors.toMap(e -> Pattern.compile(e.getKey()), Map.Entry::getValue));
	}

	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
		if (responseContext.getStatus() >= 200 && responseContext.getStatus() < 300) {
			final var hooks = self.findAll();
			final var path = requestContext.getUriInfo().getPath();
			if (hooks.entrySet().stream().anyMatch(e -> e.getKey().matcher(path).matches())) {
				execute(new HookProcessRunnable(objectMapper, hooks, requestContext, responseContext,
						((AbstractPropertiesImpl) requestContext).getMessage().getExchange()));
			}
		}
	}

}
