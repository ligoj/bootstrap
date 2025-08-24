/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.cxf.jaxrs.impl.AbstractPropertiesImpl;
import org.ligoj.bootstrap.core.NamedBean;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.AbstractMapper;
import org.ligoj.bootstrap.dao.system.SystemHookRepository;
import org.ligoj.bootstrap.model.system.HookMatch;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.ligoj.bootstrap.resource.system.hook.HookProcessRunnable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.cache.annotation.CacheResult;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * A response filter able to transform Null results to 404 HTTP response when
 */
@Provider
@Slf4j
public class HookResponseFilter extends AbstractMapper implements ContainerResponseFilter {

	@Autowired
	protected SystemHookRepository repository;

	@Autowired
	private HookResponseFilter self = this;

	@Autowired
	private ObjectMapperTrim objectMapper;

	@Autowired
	private ConfigurationResource configurationResource;

	void execute(final HookProcessRunnable runnable) {
		CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(runnable);
	}

	/**
	 * Return cached hooks grouped by matching patterns.
	 *
	 * @return cached hooks grouped by matching patterns.
	 */
	@CacheResult(cacheName = "hooks")
	public Map<Pattern, List<SystemHookParse>> findAll() {
		final var patterns = repository.findAll().stream().map(
						h -> {
							final var hp = new SystemHookParse();
							try {
								hp.setMatchObject(objectMapper.readValue(h.getMatch(), HookMatch.class));
								NamedBean.copy(h, hp);
								hp.setCommand(h.getCommand());
								hp.setMatch(h.getMatch());
								hp.setWorkingDirectory(h.getWorkingDirectory());
								hp.setTimeout(h.getTimeout());
								hp.setInject(h.getInject());
							} catch (final IOException ioe) {
								// Ignore
							}
							return hp;
						}
				).filter(hp -> hp.getMatchObject() != null)
				.filter(h -> {
					try {
						Pattern.compile(h.getMatchObject().getPath());
						return true;
					} catch (PatternSyntaxException e) {
						log.warn("Invalid path expression in hook {}", h.getName(), e);
						return false;
					}
				})
				.collect(Collectors.groupingBy(h -> h.getMatchObject().getPath()));
		return patterns.entrySet().stream().collect(Collectors.toMap(e -> Pattern.compile(e.getKey()), Map.Entry::getValue));
	}

	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
		try {
			filterUnSafe(requestContext, responseContext);
		} catch (final Exception e) {
			// Log only errors without interrupting the main flow
			log.warn("Hook filtering failed. Partially or no triggered hooks", e);
		}
	}

	void filterUnSafe(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
		if (responseContext.getStatus() >= 200 && responseContext.getStatus() < 300) {
			final var hooksByPath = self.findAll();
			final var path = requestContext.getUriInfo().getPath();
			final var hooks = hooksByPath.entrySet().stream()
					.filter(e -> e.getKey().matcher(path).matches())
					.flatMap(e -> e.getValue().stream()
							.filter(h -> h.getMatchObject().getMethod() == null
									|| Strings.CI.equals(h.getMatchObject().getMethod(), requestContext.getMethod()))).toList();
			if (!hooks.isEmpty()) {
				final var now = DateFormatUtils.formatUTC(new Date(), DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern());
				execute(new HookProcessRunnable(now, objectMapper, hooks, requestContext, responseContext,
						((AbstractPropertiesImpl) requestContext).getMessage().getExchange(),
						requestContext.getSecurityContext().getUserPrincipal(),
						configurationResource));
			}
		}
	}
}
