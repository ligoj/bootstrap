package org.ligoj.bootstrap.resource.system.hook;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.cxf.message.Exchange;
import org.ligoj.bootstrap.model.system.SystemHook;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@AllArgsConstructor
public class HookProcessRunnable implements Runnable {

	static final Base64 BASE64_CODEC = new Base64(0);

	private final String now;
	private final ObjectMapper objectMapper;
	private final Map<Pattern, List<SystemHook>> hooks;
	private final ContainerRequestContext requestContext;
	private final ContainerResponseContext responseContext;
	private final Exchange exchange;
	private final Principal principal;

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
		log.info("Executing hook {} -> {}", path, h.getName());
		try {
			// Create Map object

			final var payload = new HashMap<>(Map.of(
					"now", now,
					"path", requestContext.getUriInfo().getPath(),
					"method", requestContext.getMethod(),
					"api", exchange.get("org.apache.cxf.resource.operation.name"),
					"params", exchange.getInMessage().getContent(List.class)
			));
			payload.put("user", Optional.ofNullable(principal).map(Principal::getName).orElse(null));
			payload.put("result", responseContext.getEntity());
			final var payloadJson = objectMapper.writeValueAsString(payload);
			final var payload64 = BASE64_CODEC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
			final var pb = newBuilder(h);
			pb.environment().put("PAYLOAD", payload64);
			final var process = pb.start();
			process.getInputStream().transferTo(out);

			// Wait and get the code
			final var code = process.waitFor();
			out.flush();
			log.info("Hook {} -> {}, code: {}", path, h.getName(), code);
		} catch (final Exception ex) {
			log.error("Hook {} -> {} failed", path, h.getName(), ex);
		}
	}

	private void process() {
		final var path = requestContext.getUriInfo().getPath();
		hooks.entrySet().stream()
				.filter(e -> e.getKey().matcher(path).matches())
				.forEach(e -> e.getValue().forEach(h -> process(path, h, System.out)));
	}
}