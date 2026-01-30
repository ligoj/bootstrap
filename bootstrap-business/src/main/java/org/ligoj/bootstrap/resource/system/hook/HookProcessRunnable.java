/*
 * Licensed under MIT (https://github.com/ligoj/ligoj/blob/master/LICENSE)
 */
package org.ligoj.bootstrap.resource.system.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.ligoj.bootstrap.model.system.SystemHook;
import org.ligoj.bootstrap.resource.system.configuration.ConfigurationResource;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Runnable dedicated to hook processor.
 */
@Slf4j
@AllArgsConstructor
public class HookProcessRunnable implements Runnable {

	static final Base64 BASE64_CODEC = Base64.builder().setLineLength(0).get();
	static final int DEFAULT_TIMEOUT = Integer.parseInt(System.getProperty("LIGOJ_HOOK_TIMEOUT", "30"), 10);

	private final Exchange exchange;
	private final String method;
	private final String path;
	private final Principal principal;
	private final Object response;
	private final String now;
	private final ObjectMapper objectMapper;
	private final SystemHook hook;
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

	/**
	 * Ignored class from payload.
	 */
	private static final Class<?>[] IGNORED_CLASSES = {UriInfo.class, SecurityContext.class, ServletConfig.class,
			ServletRequest.class, ServletResponse.class, InputStream.class, ApplicationContext.class};

	/**
	 * Convert complex or Servlet like technical object to their class name only.
	 */
	private Object convertForPayload(Object parameter) {
		if (Arrays.stream(IGNORED_CLASSES).anyMatch(c -> parameter != null && c.isAssignableFrom(parameter.getClass()))) {
			// This parameter is dropped
			return "<" + parameter.getClass().getSimpleName() + ">";
		}
		return parameter;
	}

	/**
	 * Output stream that captures the content up to a limit.
	 */
	static class LimitCaptureOutputStream extends OutputStream {
		private final OutputStream out;
		private final ByteArrayOutputStream captured;
		private final int limit;

		LimitCaptureOutputStream(final OutputStream out, final ByteArrayOutputStream captured, final int limit) {
			this.out = out;
			this.captured = captured;
			this.limit = limit;
		}

		@Override
		public void write(final int b) throws IOException {
			out.write(b);
			if (captured.size() < limit) {
				captured.write(b);
			}
		}

		@Override
		public void write(final byte[] b, final int off, final int len) throws IOException {
			out.write(b, off, len);
			if (captured.size() < limit) {
				captured.write(b, off, Math.min(len, limit - captured.size()));
			}
		}
	}

	/**
	 * Process the hook.
	 *
	 * @param path The path of the resource.
	 * @param h    The hook configuration.
	 * @param out  The output stream to write the process output to.
	 */
	void process(final String path, final SystemHook h, final OutputStream out) {
		if (HookResource.isForbiddenCommand(configuration, h.getCommand())) {
			markExecution(h, "SKIP", null);
			log.info("[Hook {} -> {}] Triggered but skipped because the command '{}' is not within one of allowed ${ligoj.hook.path} value", path, h.getName(), h.getCommand());
			return;
		}
		log.info("[Hook {} -> {}] Triggered", path, h.getName());
		final var start = System.currentTimeMillis();
		final var captured = new ByteArrayOutputStream();
		try {
			// Create Map object
			@SuppressWarnings("unchecked") final var params = exchange.getInMessage().getContent(List.class).stream()
					.map(this::convertForPayload).toList();
			final var timeout = ObjectUtils.getIfNull(h.getTimeout(), 0) > 0 ? h.getTimeout() : configuration.get("LIGOJ_HOOK_TIMEOUT", DEFAULT_TIMEOUT);
			final var payload = new HashMap<String, Object>();
			payload.put("now", now);
			payload.put("name", h.getName());
			payload.put("path", path);
			payload.put("method", method);
			payload.put("api", exchange.get("org.apache.cxf.resource.operation.name"));
			payload.put("inject", CollectionUtils.emptyIfNull(h.getInject()).stream().collect(Collectors.toMap(
					Function.identity(),
					name -> configuration.get(name, ""))));
			payload.put("timeout", timeout);
			payload.put("params", params);
			payload.put("user", Optional.ofNullable(principal).map(Principal::getName).orElse(null));
			payload.put("result", convertForPayload(response));

			final var payloadJson = objectMapper.writeValueAsString(payload);
			final var payload64 = BASE64_CODEC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
			final var pb = newBuilder(h);
			pb.environment().put("PAYLOAD", payload64);
			pb.redirectErrorStream(true);

			final var process = pb.start();
			final var dualOut = new LimitCaptureOutputStream(out, captured, 2048);
			process.getInputStream().transferTo(dualOut);

			// Wait and get the code up to 30s
			final var code = process.waitFor(timeout, TimeUnit.SECONDS) ? process.exitValue() : -1;
			out.flush();
			log.info("[Hook {} -> {}] Succeed, code: {}, duration: {}", path, h.getName(), code, DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));
			markExecution(h, "SUCCEED", captured.toString(StandardCharsets.UTF_8));
		} catch (final Exception ex) {
			log.error("[Hook {} -> {}] Failed, duration: {}", path, h.getName(), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start), ex);
			markExecution(h, "FAILED", captured.toString(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Update the response headers with the hook execution status and message.
	 *
	 * @param hook    The hook configuration.
	 * @param status  The execution status.
	 * @param message The execution message (optional).
	 */
	private void markExecution(SystemHook hook, String status, String message) {
		if (hook.getDelay() > 0) {
			// Ignore response update on asynchronous hooks
			return;
		}

		@SuppressWarnings("unchecked")
		var responseHeaders = (MetadataMap<String, Object>) exchange.getOutMessage().get(Message.PROTOCOL_HEADERS);
		if (responseHeaders == null) {
			responseHeaders = new MetadataMap<>();
			exchange.getOutMessage().put(Message.PROTOCOL_HEADERS, responseHeaders);
		}
		final var hookName = hook.getName().replaceAll("[\\s\\W]", "-");
		responseHeaders.putSingle("X-Ligoj-Hook-" + hookName, status);
		if (StringUtils.isNotEmpty(message)) {
			responseHeaders.putSingle("X-Ligoj-Hook-" + hookName + "-Message", message.replaceAll("[\r\n]", " "));
		}
	}

	/**
	 * Process the hook with the current context.
	 */
	private void process() {
		process(path, hook, System.out);
	}
}