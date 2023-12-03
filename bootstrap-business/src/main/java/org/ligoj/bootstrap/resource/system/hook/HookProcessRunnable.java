/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.cxf.message.Exchange;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Runnable dedicated to hook processor.
 */
@Slf4j
@AllArgsConstructor
public class HookProcessRunnable implements Runnable {

	static final Base64 BASE64_CODEC = new Base64(0);
	static final int DEFAULT_TIMEOUT = Integer.parseInt(System.getProperty("LIGOJ_HOOK_TIMEOUT", "30"), 10);

	private final String now;
	private final ObjectMapper objectMapper;
	private final Map<Pattern, List<SystemHook>> hooks;
	private final ContainerRequestContext requestContext;
	private final ContainerResponseContext responseContext;
	private final Exchange exchange;
	private final Principal principal;
	private final ConfigurationResource configuration;

	@Override
	public void run() {
		process();
	}

	/**
	 * A new {@link ProcessBuilder} with the given arguments
	 *
	 * @param hook The hook configuration.
	 * @return The new {@link ProcessBuilder} instance.
	 */
	ProcessBuilder newBuilder(final SystemHook hook) {
		return new ProcessBuilder(ArrayUtils.addAll(hook.getCommand().split(" "))).directory(new File(hook.getWorkingDirectory()));
	}

	void process(final String path, final SystemHook h, final OutputStream out) {
		log.info("[Hook {} -> {}] Triggered", path, h.getName());
		final var start = System.currentTimeMillis();
		try {
			// Create Map object
			@SuppressWarnings("unchecked") final var params = exchange.getInMessage().getContent(List.class).stream()
					.filter(p -> !(p instanceof UriInfo || p instanceof SecurityContext)).collect(Collectors.toList());
			final var timeout = ObjectUtils.defaultIfNull(h.getTimeout(), 0) > 0 ? h.getTimeout() : configuration.get("LIGOJ_HOOK_TIMEOUT", DEFAULT_TIMEOUT);
			final var payload = new HashMap<>(Map.of(
					"now", now,
					"name", h.getName(),
					"path", requestContext.getUriInfo().getPath(),
					"method", requestContext.getMethod(),
					"api", exchange.get("org.apache.cxf.resource.operation.name"),
					"inject",
					CollectionUtils.emptyIfNull(h.getInject()).stream().collect(Collectors.toMap(
							Function.identity(),
							name -> configuration.get(name, ""))),

					"timeout", timeout,
					"params", params
			));
			payload.put("user", Optional.ofNullable(principal).map(Principal::getName).orElse(null));
			payload.put("result", responseContext.getEntity());
			final var payloadJson = objectMapper.writeValueAsString(payload);
			final var payload64 = BASE64_CODEC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
			final var pb = newBuilder(h);
			pb.environment().put("PAYLOAD", payload64);

			final var process = pb.start();
			process.getInputStream().transferTo(out);

			// Wait and get the code up to 30s
			final var code = process.waitFor(timeout, TimeUnit.SECONDS) ? process.exitValue() : -1;
			out.flush();
			log.info("[Hook {} -> {}] Succeed, code: {}, duration: {}", path, h.getName(), code, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));
		} catch (final Exception ex) {
			log.error("[Hook {} -> {}] Failed, duration: {}", path, h.getName(), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start), ex);
		}
	}

	private void process() {
		final var path = requestContext.getUriInfo().getPath();
		hooks.entrySet().stream()
				.filter(e -> e.getKey().matcher(path).matches())
				.forEach(e -> e.getValue().forEach(h -> process(path, h, System.out)));
	}
}