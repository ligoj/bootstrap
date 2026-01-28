/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.core.resource.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.cxf.message.Exchange;
import org.ligoj.bootstrap.core.NamedBean;
import org.ligoj.bootstrap.core.json.ObjectMapperTrim;
import org.ligoj.bootstrap.core.resource.filter.SystemHookParse;
import org.ligoj.bootstrap.dao.system.SystemHookRepository;
import org.ligoj.bootstrap.model.system.HookMatch;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.ligoj.bootstrap.resource.system.hook.HookProcessRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.annotation.CacheResult;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * System hook configuration and filter.
 */
@Component
@Slf4j
public class HookConfiguration {

	@Autowired
	private SystemHookRepository repository;

	@Autowired
	private ObjectMapperTrim objectMapper;

	@Autowired
	private ConfigurationResource configurationResource;

	@Autowired
	private HookConfiguration self;

	/**
	 * Return cached hooks grouped by matching patterns.
	 *
	 * @return cached hooks grouped by matching patterns.
	 */
	@CacheResult(cacheName = "hooks")
	public Map<Pattern, List<SystemHookParse>> findAll() {
		// Get all hooks and parse them
		final var patterns = repository
				.findAll().stream()
				.map(this::parseHook)
				.filter(hp -> hp.getMatchObject() != null)
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

	/**
	 * Parse the hook entity
	 */
	private SystemHookParse parseHook(SystemHook h) {
		final var hp = new SystemHookParse();
		try {
			NamedBean.copy(h, hp);
			hp.setMatch(h.getMatch());
			hp.setMatchObject(objectMapper.readValue(h.getMatch(), HookMatch.class));
			hp.setCommand(h.getCommand());
			hp.setWorkingDirectory(h.getWorkingDirectory());
			hp.setTimeout(Optional.ofNullable(h.getTimeout()).orElse(10));
			hp.setInject(h.getInject());
			hp.setDelay(Optional.ofNullable(h.getDelay()).orElse(1));
		} catch (final IOException ioe) {
			// Ignore
			hp.setMatchObject(null);
		}
		return hp;
	}

	public void process(final Exchange exchange, String method, final String path, final Principal principal, final Object response,
			final Predicate<SystemHook> filter,
			final BiConsumer<SystemHook, HookProcessRunnable> processor) {
		try {
			filterUnSafe(exchange, method, path, principal, response, filter, processor);
		} catch (final Exception e) {
			// Log only errors without interrupting the main flow
			log.warn("Hook filtering failed. Partially or no triggered hooks {} {}", method, path, e);
		}
	}

	void filterUnSafe(final Exchange exchange, String method, final String path, final Principal principal, final Object response,
			final Predicate<SystemHook> filter,
			final BiConsumer<SystemHook, HookProcessRunnable> processor) {
		self.findAll().entrySet().stream()
				.filter(e -> e.getKey().matcher(path).matches())
				.flatMap(e -> e.getValue().stream()
						.filter(filter)
						.filter(h -> h.getMatchObject().getMethod() == null
								|| Strings.CI.equals(h.getMatchObject().getMethod(), method)))
				.forEach(hook -> {
					final var now = DateFormatUtils.formatUTC(new Date(), DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern());
					processor.accept(hook, new HookProcessRunnable(exchange, method, path,
							principal, response, now, objectMapper, hook, configurationResource));
				});
	}

}
